package dev.inreach.jobs;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "crawled_jobs")
public class Job {
  @Id public Long id;
  public String title;

  @Column(name = "source_url")
  public String sourceUrl;

  public String provider;

  @Column(name = "board_token")
  public String boardToken;

  @Column(name = "external_job_id")
  public String externalJobId;

  public String status;

  @Column(name = "last_seen_at")
  public Instant lastSeenAt;

  protected Job() {}

  public record Dto(
      String id,
      String title,
      String source_url,
      String provider,
      String board_token,
      String external_job_id,
      Instant last_seen_at) {}

  public Dto dto() {
    return new Dto(
        id.toString(), title, sourceUrl, provider, boardToken, externalJobId, lastSeenAt);
  }
}
