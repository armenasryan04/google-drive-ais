package com.samplemission.collectcvsfromgoogledrive.model.mapper;

import com.samplemission.collectcvsfromgoogledrive.model.dto.user.ResponsibleHrForRegistrationDto;
import com.samplemission.collectcvsfromgoogledrive.model.entity.ResponsibleHr;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

  ResponsibleHr toResponsibleHr(ResponsibleHrForRegistrationDto createResponsibleHrRequestDto);

  ResponsibleHrForRegistrationDto toResponsibleHrDto(ResponsibleHr responsibleHr);
}
