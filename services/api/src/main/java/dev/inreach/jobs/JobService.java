package dev.inreach.jobs;

import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

@org.springframework.stereotype.Service
public class JobService {
  private final JobRepository jobs;
  private final JdbcTemplate db;

  public JobService(JobRepository jobs, JdbcTemplate db) {
    this.jobs = jobs;
    this.db = db;
  }

  public Map<String, Object> list(String q, int page, int pageSize) {
    String query = q.trim();
    String pattern =
        "%"
            + query
                .toLowerCase(Locale.ROOT)
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_")
            + "%";
    Specification<Job> spec =
        (root, cq, cb) ->
            cb.and(
                cb.notEqual(root.get("status"), "closed"),
                cb.like(cb.lower(root.get("title")), pattern, '!'));
    long total = jobs.count(spec);
    int pages = (int) Math.max(1, (total + pageSize - 1) / pageSize);
    int current = Math.min(page, pages);
    var rows =
        jobs.findAll(
            spec,
            PageRequest.of(
                current - 1,
                pageSize,
                Sort.by(Sort.Order.desc("lastSeenAt"), Sort.Order.desc("id"))));
    return Map.of(
        "jobs",
        rows.stream().map(Job::dto).toList(),
        "total",
        total,
        "page",
        current,
        "pages",
        pages,
        "query",
        query,
        "pageSize",
        pageSize);
  }

  public Map<String, Object> stats() {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put(
        "active",
        db.queryForObject(
            "select count(*) from crawled_jobs where status <> 'closed'", Long.class));
    out.put("sources", db.queryForObject("select count(*) from career_sources", Long.class));
    out.put(
        "last_import",
        db.queryForObject(
            "select max(imported_at) from crawl_imports", java.time.OffsetDateTime.class));
    return out;
  }

  public Job.Dto detail(long id) {
    return jobs.findById(id)
        .filter(j -> !j.status.equals("closed"))
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stelle nicht gefunden"))
        .dto();
  }
}
