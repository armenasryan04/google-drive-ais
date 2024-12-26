package com.samplemission.collectcvsfromgoogledrive.model.entity;

import com.samplemission.collectcvsfromgoogledrive.model.enums.HrRole;
import javax.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tb_responsible_hr")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ResponsibleHr extends PersistableEntity {
  @Column(name = "name", nullable = false, length = 50)
  private String name;

  @Column(name = "surname", length = 50)
  private String surname;

  @Column(name = "email", nullable = false, length = 50)
  private String email;

  @Column(name = "username", nullable = false, length = 50)
  private String username;

  @Column(name = "password", nullable = false, length = 250)
  private String password;

  @Column(name = "role", nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private HrRole role;

  public String getLogin() {
    return this.email + " " + this.username;
  }
}
