package dev.inreach.ingestion;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/ingestion/greenhouse")
public class IngestionController {
  private final IngestionService service;

  public IngestionController(IngestionService service) {
    this.service = service;
  }

  @PostMapping
  public Map<String, Object> ingest(
      @RequestHeader(value = "Authorization", defaultValue = "") String auth,
      @Valid @RequestBody IngestionService.Feed feed) {
    return service.ingest(auth, feed);
  }
}
