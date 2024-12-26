package com.samplemission.collectcvsfromgoogledrive.utill;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TimeUtils {
  public static LocalDateTime getCurrentDateTime() {
    return LocalDateTime.now();
  }

  public static LocalDate getCurrentDate() {
    return LocalDate.now();
  }
}
