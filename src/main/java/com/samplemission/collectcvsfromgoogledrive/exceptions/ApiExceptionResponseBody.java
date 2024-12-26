package com.samplemission.collectcvsfromgoogledrive.exceptions;

import com.samplemission.collectcvsfromgoogledrive.utill.TimeUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiExceptionResponseBody {
  private final UUID id = UUID.randomUUID();
  private final LocalDateTime timestamp = TimeUtils.getCurrentDateTime();
  private final List<String> details;
}
