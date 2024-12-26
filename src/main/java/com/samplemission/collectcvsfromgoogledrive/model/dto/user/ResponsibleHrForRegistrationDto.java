package com.samplemission.collectcvsfromgoogledrive.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponsibleHrForRegistrationDto {
  private String name;
  private String username;
  private String email;
  private String password;
  private String surname;
}
