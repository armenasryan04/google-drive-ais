package com.samplemission.collectcvsfromgoogledrive.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EducationLevelEnum {
  HIGHER_INCOMPLETE("неоконченное высшее"),
  HIGHER("высшее"),
  BACHELOR("бакалавр"),
  MAGISTER("магистр"),
  CANDIDATE_SCIENCES("кандидат наук"),
  DOCTOR_SCIENCE("доктор наук"),
  SECONDARY_SPECIALIZED("среднее специальное"),
  SECONDARY("среднее");

  private final String description;

  public String getValue() {
    return this.name().toLowerCase();
  }
}
