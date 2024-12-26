package com.samplemission.collectcvsfromgoogledrive.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RelocationEnum {
  AVAILABLE("(Гото(в|ва)) к переезду"),
  DESIRABLE("Хочу переехать"),
  IMPOSSIBLE("Не .+(?= к переезду)");
  private final String regex;

  public String getValue() {
    return this.name().toLowerCase();
  }
}
