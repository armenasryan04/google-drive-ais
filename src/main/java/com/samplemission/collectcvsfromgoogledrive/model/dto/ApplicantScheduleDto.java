package com.samplemission.collectcvsfromgoogledrive.model.dto;

import com.samplemission.collectcvsfromgoogledrive.model.enums.ScheduleEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicantScheduleDto {

  ScheduleEnum scheduleType;

  public ApplicantScheduleDto() {}

  public ApplicantScheduleDto(ScheduleEnum scheduleType) {
    this.scheduleType = scheduleType;
  }
}
