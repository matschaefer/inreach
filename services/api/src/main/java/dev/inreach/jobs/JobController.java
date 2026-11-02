package dev.inreach.jobs;

import jakarta.validation.constraints.*;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/jobs")
public class JobController {
  private final JobService service;

  public JobController(JobService service) {
    this.service = service;
  }

  @GetMapping
  public Map<String, Object> list(
      @RequestParam(defaultValue = "") @Size(max = 120) String q,
      @RequestParam(defaultValue = "1") @Min(1) @Max(10000) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize) {
    return service.list(q, page, pageSize);
  }

  @GetMapping("/stats")
  public Map<String, Object> stats() {
    return service.stats();
  }

  @GetMapping("/{id}")
  public Job.Dto detail(@PathVariable @Min(1) long id) {
    return service.detail(id);
  }
}
