package com.samplemission.collectcvsfromgoogledrive.security;

import com.samplemission.collectcvsfromgoogledrive.model.entity.ResponsibleHr;
import com.samplemission.collectcvsfromgoogledrive.repository.ResponsibleHrRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final ResponsibleHrRepository responsibleHrRepository;

  @Override
  public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
    Optional<ResponsibleHr> userFromDB =
        responsibleHrRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
    if (userFromDB.isEmpty()) {
      throw new UsernameNotFoundException(usernameOrEmail + " not found");
    }
    return new CurrentUser(userFromDB.get());
  }
}
