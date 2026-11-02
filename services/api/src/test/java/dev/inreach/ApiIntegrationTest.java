package dev.inreach;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.*;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;

class ApiIntegrationTest {
  static org.testcontainers.postgresql.PostgreSQLContainer postgres =
      new org.testcontainers.postgresql.PostgreSQLContainer("postgres:16-alpine");
  static org.springframework.context.ConfigurableApplicationContext context;
  static int port;
  JdbcTemplate db;

  @BeforeAll
  static void start() {
    postgres.start();
    try {
      context =
          new org.springframework.boot.builder.SpringApplicationBuilder(InreachApplication.class)
              .run(
                  "--spring.datasource.url=" + postgres.getJdbcUrl(),
                  "--spring.datasource.username=" + postgres.getUsername(),
                  "--spring.datasource.password=" + postgres.getPassword(),
                  "--server.port=0",
                  "--INREACH_INGESTION_TOKEN=test-token-long-enough-for-ingestion");
      port = Integer.parseInt(context.getEnvironment().getProperty("local.server.port"));
    } catch (RuntimeException e) {
      postgres.stop();
      throw e;
    }
  }

  @AfterAll
  static void stop() {
    if (context != null) context.close();
    postgres.stop();
  }

  @BeforeEach
  void setup() {
    db = context.getBean(JdbcTemplate.class);
  }

  HttpClient client = HttpClient.newHttpClient();

  HttpResponse<String> get(String path) throws Exception {
    return client.send(
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).build(),
        HttpResponse.BodyHandlers.ofString());
  }

  HttpResponse<String> post(String body, boolean auth) throws Exception {
    var b =
        HttpRequest.newBuilder(
                URI.create("http://127.0.0.1:" + port + "/internal/ingestion/greenhouse"))
            .header("Content-Type", "application/json");
    if (auth) b.header("Authorization", "Bearer test-token-long-enough-for-ingestion");
    return client.send(
        b.POST(HttpRequest.BodyPublishers.ofString(body)).build(),
        HttpResponse.BodyHandlers.ofString());
  }

  @Test
  void endToEnd() throws Exception {
    assertEquals(200, get("/api/health").statusCode());
    String feed =
        """
        {"board":"fixture","complete":true,"rawCount":2,"jobs":[{"id":"123","title":"Security Engineer","url":"https://example.com/job?gh_jid=123&utm_source=test"},{"id":"123","title":"Security Engineer","url":"https://example.com/job?gh_jid=123"}]}
        """;
    assertEquals(401, post(feed, false).statusCode());
    assertEquals(200, post(feed, true).statusCode());
    assertEquals(200, post(feed, true).statusCode());
    assertEquals(
        1,
        db.queryForObject(
            "select count(*) from crawled_jobs where board_token='fixture'", Integer.class));
    assertTrue(get("/api/jobs?q=Security&pageSize=1").body().contains("Security Engineer"));
    assertTrue(get("/api/jobs?q=%25").body().contains("\"total\":0"));
    assertEquals(400, get("/api/jobs?pageSize=101").statusCode());
    assertEquals(400, get("/api/jobs?page=x").statusCode());
    assertEquals(404, get("/api/jobs/99999").statusCode());
    Long id =
        db.queryForObject(
            "select id from crawled_jobs where board_token='fixture' limit 1", Long.class);
    assertEquals(200, get("/api/jobs/" + id).statusCode());
    assertEquals(200, get("/api/jobs/stats").statusCode());
    assertEquals(200, post(feed.replace("true", "false"), true).statusCode());
    assertEquals(
        400,
        post("{\"board\":\"fixture\",\"complete\":true,\"rawCount\":0,\"jobs\":[]}", true)
            .statusCode());
    assertEquals(
        "active",
        db.queryForObject(
            "select status from crawled_jobs where board_token='fixture' limit 1", String.class));
  }

  @Test
  void paginationIsStableAndDoesNotOverlap() throws Exception {
    db.update(
        "insert into"
            + " crawled_jobs(provider,board_token,external_job_id,title,source_url,raw_payload)"
            + " select 'greenhouse','pagination',cast(n as"
            + " text),'PaginationFixture','https://example.com/job','{}'::jsonb from"
            + " generate_series(1,25) n");
    var mapper = tools.jackson.databind.json.JsonMapper.builder().build();
    var first = mapper.readTree(get("/api/jobs?q=PaginationFixture&pageSize=20&page=1").body());
    var second = mapper.readTree(get("/api/jobs?q=PaginationFixture&pageSize=20&page=2").body());
    assertEquals(25, first.get("total").asInt());
    assertEquals(20, first.get("jobs").size());
    assertEquals(5, second.get("jobs").size());
    var ids = new java.util.HashSet<String>();
    for (var row : first.get("jobs")) assertTrue(ids.add(row.get("id").asText()));
    for (var row : second.get("jobs")) assertTrue(ids.add(row.get("id").asText()));
    assertEquals(first, getTree(mapper, "/api/jobs?q=PaginationFixture&pageSize=20&page=1"));
  }

  private tools.jackson.databind.JsonNode getTree(
      tools.jackson.databind.json.JsonMapper mapper, String path) throws Exception {
    return mapper.readTree(get(path).body());
  }
}
