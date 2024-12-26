package com.samplemission.collectcvsfromgoogledrive.repository;

import com.samplemission.collectcvsfromgoogledrive.model.entity.ResponsibleHr;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponsibleHrRepository extends JpaRepository<ResponsibleHr, Long> {
  Optional<ResponsibleHr> findByUsernameOrEmail(String username, String email);
}
