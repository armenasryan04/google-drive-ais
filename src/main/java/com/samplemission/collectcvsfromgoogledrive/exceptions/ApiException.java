package com.samplemission.collectcvsfromgoogledrive.exceptions;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

public abstract class ApiException extends RuntimeException {
  private final ApiExceptionResponseBody body;

  public ApiException(String message) {
    this(List.of(message));
  }

  public ApiException(RuntimeException e, String message) {
    this(List.of(message));
  }

  public ApiException(List<String> messages) {
    body = new ApiExceptionResponseBody(messages);
  }

  @NonNull
  public final ApiExceptionResponseBody getResponseBody() {
    return body;
  }

  @NonNull
  public abstract HttpStatus getStatus();

  @Override
  public String getMessage() {
    return getResponseBody().getDetails().toString();
  }
}
