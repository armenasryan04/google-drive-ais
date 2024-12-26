package com.samplemission.collectcvsfromgoogledrive.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.samplemission.collectcvsfromgoogledrive.model.enums.BusinessTripTypeEnum;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicantDto {
  private String state;
  private String surname;
  private String name;
  private String patronym;
  private String gender;
  private BigInteger salary;
  private LocalDate dateBirth;
  private String currency;
  private String country;
  private String region;
  private String city;
  private String grade;
  private String position;
  private String relocation;
  private BusinessTripTypeEnum isBusinessTrip;
  private String comment;
  private String typeLink;
  private Boolean workPermit;
  private Boolean isViewed;
  private LocalDateTime dateView;
  private Integer version;
  private NationalityDto nationality;
  private String googleLink;
  private List<ContactDto> contacts;
  private List<EducationDto> education;
  private List<LanguageDto> languages;
  private List<EmploymentDto> employments;
  private List<SkillDto> skills;
  private List<NationalityDto> nationalities;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
  private LocalDateTime timeCreateResume;

  private List<ApplicantScheduleDto> schedules = new ArrayList<>();
  private List<ExperienceDto> experiences;

  public void setGoogleLink(String id) {
    this.googleLink = "https://docs.google.com/document/d/" + id + "/edit";
  }
}
