package com.samplemission.collectcvsfromgoogledrive.model.entity;

import java.util.UUID;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import lombok.*;
import org.hibernate.annotations.Type;
import org.springframework.data.domain.Persistable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EqualsAndHashCode(of = "id")
public abstract class PersistableEntity implements Persistable<UUID> {
  @Id
  @GeneratedValue
  @Type(type = "uuid-char")
  private UUID id;

  @Override
  public boolean isNew() {
    return this.getId() == null;
  }
}
