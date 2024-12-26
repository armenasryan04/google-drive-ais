package com.samplemission.collectcvsfromgoogledrive.model.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class ExperienceDto {
  private LocalDate startDate;
  private LocalDate endDate;
  private String companyName;
  private String site;
  private ExperienceActivityDto activity;
  private String position;
  private String description;
}
