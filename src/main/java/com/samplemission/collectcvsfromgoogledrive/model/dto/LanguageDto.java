package com.samplemission.collectcvsfromgoogledrive.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LanguageDto {
  private String attribute;

  private String language;

  private String level;
}
