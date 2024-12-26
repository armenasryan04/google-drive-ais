package com.samplemission.collectcvsfromgoogledrive.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BusinessTripTypeEnum {
  NEVER(".+не гото(ва|в) к командировкам", "Не готов к командировкам"),
  SOMETIMES(".+гото(ва|в) к редким командировкам", "Готов к редким командировкам"),
  READY(".+гото(ва|в) к командировкам", "Готов к командировкам");

  private final String description;
  private final String correctDescription;

  private String getValue() {
    return this.name().toLowerCase();
  }
}
