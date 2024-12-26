package com.samplemission.collectcvsfromgoogledrive.model.dto;

import com.samplemission.collectcvsfromgoogledrive.model.enums.EducationMethodEnum;
import lombok.Data;

@Data
public class EducationDto {
  private String educationType;
  private String universityName;
  private String specialization;
  private String organization;
  private String faculty;
  private EducationMethodEnum method;
  private Integer endYear;
}
