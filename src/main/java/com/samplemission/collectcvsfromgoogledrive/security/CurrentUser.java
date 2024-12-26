package com.samplemission.collectcvsfromgoogledrive.security;

import com.samplemission.collectcvsfromgoogledrive.model.entity.ResponsibleHr;
import org.springframework.security.core.authority.AuthorityUtils;

public class CurrentUser extends org.springframework.security.core.userdetails.User {
  private ResponsibleHr responsibleHr;

  public CurrentUser(ResponsibleHr user) {
    super(
        user.getLogin(),
        user.getPassword(),
        AuthorityUtils.createAuthorityList(user.getRole().getRoleDescription()));
    this.responsibleHr = user;
  }

  public ResponsibleHr getUser() {
    return responsibleHr;
  }
}
