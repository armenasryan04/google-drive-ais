package com.samplemission.collectcvsfromgoogledrive.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContactTypeEnum {
  MOBILE_PHONE("(\\+7\\h\\(\\d{3}\\)\\h\\d{7})"),
  E_MAIL("([\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4})"),
  LINKED_IN("https:\\/\\/www\\.linkedin\\.com\\/in\\/\\w+");

  private final String regex;

  public String getValue() {
    return this.name().toLowerCase();
  }
}
