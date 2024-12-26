package com.samplemission.collectcvsfromgoogledrive.model.parcer.v2;

import com.samplemission.collectcvsfromgoogledrive.model.dto.ApplicantDto;
import com.samplemission.collectcvsfromgoogledrive.model.dto.ContactDto;
import com.samplemission.collectcvsfromgoogledrive.model.dto.NationalityDto;
import com.samplemission.collectcvsfromgoogledrive.model.enums.BusinessTripTypeEnum;
import com.samplemission.collectcvsfromgoogledrive.model.enums.ContactTypeEnum;
import com.samplemission.collectcvsfromgoogledrive.model.enums.RelocationEnum;
import com.samplemission.collectcvsfromgoogledrive.model.parcer.DirectoryData;
import com.samplemission.collectcvsfromgoogledrive.utill.HhConversionUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralInfoParser {

  public String nameSurnamePatronymRegex = "(.+(?=\\n(Женщина|Мужчина)))";
  public String genderRegex = "(Мужчина|Женщина)(.*родил(ся|ась)\\h(\\d{1,2}\\h[А-я]+\\h\\d{4}))?";
  public String cityRegex = "(?<=Проживает:\\h)([А-я-\\.\\h]+)";
  public String countryRegex = "(?<=Гражданство:\\h)([[А-я-\\.\\h]]+)";
  public String nationalityRegex =
      "(?<=Гражданство:\\s)(.*?)(?=\\s*,\\s*есть разрешение на работу|\\n)";
  public String relocationRegex =
      "(((Не\\hг|Г)отов)а?\\hк\\hпереезду|Хочу переехать)(:\\h([[А-я-\\.\\h]+,?]+))?,\\h((не\\hг|г)отов).*";
  public String workPermissionRegex = "(?<=(есть разрешение на работу: )).+";
  public static final int SURNAME_INDEX = 0;
  public static final int NAME_INDEX = 1;
  public static final int PATRONYM_INDEX = 2;
  public static final int SIZE_WITHOUT_PATRONYM = 2;

  private final HhConversionUtils hhConversionUtils;
  private final DateTimeFormatter localDatePatternRu;

  public void fillGeneralInfo(ApplicantDto applicantDto, String text) {
    fillNameSurnamePatronym(applicantDto, text);
    fillCountry(applicantDto, text);
    fillCity(applicantDto, text);
    fillRelocation(applicantDto, text);
    fillBioInfo(applicantDto, text);
    fillContacts(applicantDto, text);
    fillNationality(applicantDto, text);
    fillWorkPermission(applicantDto, text);
  }

  public void fillNameSurnamePatronym(ApplicantDto applicantDto, String text) {
    String nameSurnamePatronym = hhConversionUtils.findByRegex(nameSurnamePatronymRegex, text);
    String[] parts = nameSurnamePatronym.split("\\s");
    applicantDto.setSurname(parts[SURNAME_INDEX]);
    applicantDto.setName(parts[NAME_INDEX]);
    if (parts.length > SIZE_WITHOUT_PATRONYM) {
      applicantDto.setPatronym(parts[PATRONYM_INDEX]);
    }
  }

  public void fillCountry(ApplicantDto applicantDto, String text) {
    String region = hhConversionUtils.findByRegex(countryRegex, text);
    String country = DirectoryData.COUNTRY.get(region);
    if (!country.trim().isEmpty()) {
      applicantDto.setCountry(country);
    } else {
      log.info("Cant import country for applicant, no proper value found");
    }
  }

  public void fillCity(ApplicantDto applicantDto, String text) {
    String city = hhConversionUtils.findByRegex(cityRegex, text);
    applicantDto.setCity(city);
  }

  public void fillRelocation(ApplicantDto applicantDto, String text) {
    String relocationAndBusinessTrip = hhConversionUtils.findByRegex(relocationRegex, text);
    applicantDto.setRelocation(getRelocation(relocationAndBusinessTrip));
    applicantDto.setIsBusinessTrip(getBusinessTrip(relocationAndBusinessTrip));
  }

  private static BusinessTripTypeEnum getBusinessTrip(String relocationAndBusinessTrip) {
    return Arrays.stream(BusinessTripTypeEnum.values())
        .filter(e -> relocationAndBusinessTrip.matches(e.getDescription()))
        .findFirst()
        .orElse(null);
  }

  private String getRelocation(String text) {
    return Arrays.stream(RelocationEnum.values())
        .filter(
            relocationEnum ->
                hhConversionUtils.checkIfContainsRegex(relocationEnum.getRegex(), text))
        .findFirst()
        .map(RelocationEnum::getValue)
        .orElseGet(
            () -> {
              log.info("Relocation info for applicant is empty");
              return null;
            });
  }

  public void fillBioInfo(ApplicantDto applicantDto, String text) {
    String bioInfo = hhConversionUtils.findByRegex(genderRegex, text);
    String gender = hhConversionUtils.findByRegex("(Мужчина|Женщина)", bioInfo);
    String genderValue = DirectoryData.GENDER.get(gender);
    if (genderValue == null) throw new RuntimeException();
    applicantDto.setGender(genderValue);
    String dateOfBirthday = hhConversionUtils.findByRegex("\\d+ [а-яА-Я]+ \\d+", bioInfo);
    if (dateOfBirthday != null) {
      LocalDate ld = LocalDate.parse(dateOfBirthday, localDatePatternRu);
      applicantDto.setDateBirth(ld);
    }
  }

  public void fillContacts(ApplicantDto applicantDto, String text) {
    List<String> probableContacts = List.of(text.split("\n"));
    if (probableContacts.isEmpty()) {
      log.info("Applicant CV without suitable contacts, cant read contacts");
      return;
    }
    applicantDto.setContacts(
        Arrays.stream(ContactTypeEnum.values())
            .flatMap(
                type ->
                    probableContacts.stream()
                        .filter(
                            contact ->
                                hhConversionUtils.checkIfContainsRegex(type.getRegex(), contact))
                        .map(
                            contact ->
                                getApplicantContactDto(
                                    type.getValue(),
                                    hhConversionUtils.findByRegex(type.getRegex(), contact))))
            .toList());
  }

  private ContactDto getApplicantContactDto(String type, String value) {
    var contact = new ContactDto();
    contact.setContactType(type);
    if (Objects.equals(type, ContactTypeEnum.MOBILE_PHONE.getValue())) {
      value = value.replaceAll("[^\\d+.]", "");
    }
    contact.setValue(value);
    return contact;
  }

  public void fillNationality(ApplicantDto applicantDto, String text) {
    String nationality = hhConversionUtils.findByRegex(nationalityRegex, text);
    applicantDto.setNationalities(
        DirectoryData.COUNTRY.entrySet().stream()
            .filter(entry -> nationality.contains(entry.getKey()))
            .map(entry -> new NationalityDto(entry.getValue()))
            .toList());
  }

  public void fillWorkPermission(ApplicantDto applicantDto, String text) {
    String workPermission = hhConversionUtils.findByRegex(workPermissionRegex, text);
    if (workPermission != null && workPermission.contains("Россия")) {
      applicantDto.setWorkPermit(true);
    }
  }
}
