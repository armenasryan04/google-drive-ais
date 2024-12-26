package com.samplemission.collectcvsfromgoogledrive.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScheduleEnum {
  FULL_DAY("Полный день"),
  SHIFT_SCHEDULE("Сменный график"),
  FLEXIBLE_SCHEDULE("Гибкий график"),
  REMOTE_WORK("Удаленная работа"),
  SHIFT_METHOD("Вахтовый метод");

  private final String name;

  @JsonValue
  public String getValue() {
    return this.name().toLowerCase();
  }
}
