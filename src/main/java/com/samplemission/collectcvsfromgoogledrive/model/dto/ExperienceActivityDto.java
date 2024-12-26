package com.samplemission.collectcvsfromgoogledrive.model.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ExperienceActivityDto {
  private String activityName;
  private List<String> details = new ArrayList<>();
}
