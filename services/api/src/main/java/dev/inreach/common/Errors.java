package dev.inreach.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class Errors {
  @ExceptionHandler({
    ConstraintViolationException.class,
    org.springframework.web.bind.MethodArgumentNotValidException.class,
    org.springframework.web.method.annotation.HandlerMethodValidationException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
    org.springframework.http.converter.HttpMessageNotReadableException.class
  })
  public ProblemDetail invalid(Exception e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Ungültige Anfrageparameter");
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ProblemDetail status(ResponseStatusException e) {
    return ProblemDetail.forStatusAndDetail(e.getStatusCode(), e.getReason());
  }

  @ExceptionHandler(DataAccessException.class)
  public ProblemDetail database(Exception e) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.SERVICE_UNAVAILABLE, "Daten vorübergehend nicht verfügbar");
  }
}
