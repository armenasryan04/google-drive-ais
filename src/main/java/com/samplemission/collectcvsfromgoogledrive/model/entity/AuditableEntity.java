package com.samplemission.collectcvsfromgoogledrive.model.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public abstract class AuditableEntity extends PersistableEntity {
  @CreatedDate
  @Column(name = "time_create", nullable = false, updatable = false)
  private LocalDateTime timeCreate;

  @LastModifiedDate
  @Column(name = "time_update")
  private LocalDateTime timeUpdate;
}
