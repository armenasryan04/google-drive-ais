package com.samplemission.collectcvsfromgoogledrive.model.enums;

public enum EducationMethodEnum {
  EDUCATION,
  COURSE,
  TEST,
  CERTIFICATE;

  private String getValue() {
    return this.name().toLowerCase();
  }
}
