package dev.inreach.ingestion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service
public class IngestionService {
  private final JdbcTemplate db;
  private final String token;

  public IngestionService(JdbcTemplate db, @Value("${INREACH_INGESTION_TOKEN:}") String token) {
    this.db = db;
    this.token = token;
  }

  public record Item(
      @NotBlank @Pattern(regexp = "[0-9]{1,30}") String id,
      @NotBlank @Size(max = 1000) String title,
      @NotBlank @Size(max = 4096) String url) {}

  public record Feed(
      @NotBlank @Pattern(regexp = "[a-zA-Z0-9_-]{2,80}") String board,
      boolean complete,
      @Min(0) int rawCount,
      @NotNull @Size(max = 50000) List<@Valid Item> jobs) {}

  @Transactional
  public Map<String, Object> ingest(String auth, Feed feed) {
    if (token.length() < 24
        || !MessageDigest.isEqual(
            auth.getBytes(StandardCharsets.UTF_8),
            ("Bearer " + token).getBytes(StandardCharsets.UTF_8)))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Import nicht autorisiert");
    if (feed.jobs().isEmpty() || feed.rawCount() != feed.jobs().size())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Leerer oder inkonsistenter Feed");
    boolean locked =
        Boolean.TRUE.equals(
            db.queryForObject(
                "select pg_try_advisory_xact_lock(hashtextextended(?,0))",
                Boolean.class,
                "greenhouse:" + feed.board()));
    if (!locked)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Quelle wird bereits importiert");
    var unique = new LinkedHashMap<String, Item>();
    for (Item item : feed.jobs()) {
      Item normalized = new Item(item.id(), item.title().trim(), canonicalUrl(item.url()));
      if (unique.containsKey(item.id()) && !unique.get(item.id()).equals(normalized))
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Widersprüchliche Job-ID");
      unique.put(item.id(), normalized);
    }
    db.update("insert into companies(name) values (?) on conflict do nothing", feed.board());
    db.update(
        "insert into career_sources(company_id,provider,board_token) select id,'greenhouse',? from"
            + " companies where name=? on conflict do nothing",
        feed.board(),
        feed.board());
    for (Item j : unique.values()) {
      db.update(
          "insert into job_status_history(job_id,old_status,new_status,reason) select"
              + " id,status,'active','reopened by observed job' from crawled_jobs where"
              + " provider='greenhouse' and board_token=? and external_job_id=? and"
              + " status<>'active'",
          feed.board(),
          j.id());
      db.update(
          "insert into"
              + " crawled_jobs(provider,board_token,external_job_id,title,source_url,raw_payload)"
              + " values ('greenhouse',?,?,?,?,jsonb_build_object('id',cast(? as text))) on"
              + " conflict(provider,board_token,external_job_id) do update set"
              + " title=excluded.title,source_url=excluded.source_url,last_seen_at=now(),status='active'",
          feed.board(),
          j.id(),
          j.title(),
          j.url(),
          j.id());
    }
    db.update(
        "insert into"
            + " crawl_imports(provider,board_token,raw_job_count,valid_job_count,duplicate_in_feed_count,truncated,source_status,complete)"
            + " values ('greenhouse',?,?,?,?,?, '200',?)",
        feed.board(),
        feed.rawCount(),
        unique.size(),
        feed.jobs().size() - unique.size(),
        !feed.complete(),
        feed.complete());
    return Map.of(
        "imported",
        unique.size(),
        "duplicates",
        feed.jobs().size() - unique.size(),
        "complete",
        feed.complete());
  }

  static String canonicalUrl(String value) {
    try {
      var u = URI.create(value);
      if (!"https".equalsIgnoreCase(u.getScheme())
          || u.getHost() == null
          || u.getUserInfo() != null
          || (u.getPort() != -1 && u.getPort() != 443)) throw new IllegalArgumentException();
      String query = u.getRawQuery();
      if (query != null)
        query =
            Arrays.stream(query.split("&"))
                .filter(
                    p -> !p.toLowerCase(Locale.ROOT).matches("(utm_[^=]*|gh_src|source|ref)=.*"))
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.joining("&"));
      return "https://"
          + u.getHost().toLowerCase(Locale.ROOT)
          + Optional.ofNullable(u.getRawPath()).orElse("")
          + (query == null || query.isEmpty() ? "" : "?" + query);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ungültige Stellen-URL");
    }
  }
}
