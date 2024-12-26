package com.samplemission.collectcvsfromgoogledrive.model.parcer.v2;

import com.samplemission.collectcvsfromgoogledrive.model.dto.ApplicantDto;
import com.samplemission.collectcvsfromgoogledrive.model.dto.EducationDto;
import com.samplemission.collectcvsfromgoogledrive.model.enums.EducationLevelEnum;
import com.samplemission.collectcvsfromgoogledrive.model.enums.EducationMethodEnum;
import com.samplemission.collectcvsfromgoogledrive.utill.HhConversionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EducationInfoParser {
  public static final int DATE_CELL_INDEX = 0;
  public static final int INFO_CELL_INDEX = 1;
  public static final int EDUCATION_TYPE_ROW_INDEX = 0;
  public static final int FACULTY_AND_SPECIALIZATION_PARAGRAPH_INDEX = 1;
  public static final int FACULTY_INDEX = 0;
  public static final int SPECIALIZATION_INDEX = 1;

  private final HhConversionUtils hhConversionUtils;

  public void fillEducationInfo(ApplicantDto applicantDto, List<XWPFTableRow> rows) {
    List<EducationDto> educationDtos = new ArrayList<>();
    educationDtos.addAll(fillEducation(rows));
    educationDtos.addAll(
        fillSection(rows, "Повышение квалификации, курсы", EducationMethodEnum.COURSE));
    educationDtos.addAll(fillSection(rows, "Тесты, экзамены", EducationMethodEnum.TEST));
    educationDtos.addAll(fillCertificate(rows));
    applicantDto.setEducation(educationDtos);
  }

  private List<EducationDto> fillEducation(List<XWPFTableRow> rows) {
    List<XWPFTableRow> foundedRows = hhConversionUtils.findRowsUpToNextTitle(rows, "Образование");
    XWPFTableRow levelRow = foundedRows.get(EDUCATION_TYPE_ROW_INDEX);
    if (levelRow.getCell(DATE_CELL_INDEX).getText().contains("Уровень")) {
      var educationDto = new EducationDto();
      educationDto.setMethod(EducationMethodEnum.EDUCATION);
      educationDto.setEducationType(getEducationType(levelRow.getCell(INFO_CELL_INDEX).getText()));
      return List.of(educationDto);
    }
    String educationType = getEducationType(levelRow.getCell(DATE_CELL_INDEX).getText());
    List<XWPFTableRow> educationRows =
        hhConversionUtils.findRowsBetween(foundedRows, levelRow, null);
    return educationRows.stream()
        .map(row -> createEducationDto(row, EducationMethodEnum.EDUCATION))
        .peek(dto -> dto.setEducationType(educationType))
        .toList();
  }

  private List<EducationDto> fillCertificate(List<XWPFTableRow> rows) {
    List<XWPFTableRow> foundedRows =
        hhConversionUtils.findRowsUpToNextTitle(rows, "Электронные сертификаты");
    List<EducationDto> certificateDtos = new ArrayList<>();
    for (XWPFTableRow row : foundedRows) {
      certificateDtos.addAll(getCertificatesFromRow(row));
    }
    return certificateDtos;
  }

  private List<EducationDto> getCertificatesFromRow(XWPFTableRow row) {
    Integer endYear = Integer.valueOf(row.getCell(DATE_CELL_INDEX).getText().trim());
    String[] certificates = row.getCell(INFO_CELL_INDEX).getText().split("\n");
    return Arrays.stream(certificates)
        .map(certificate -> createCertificateEducationDto(endYear, certificate))
        .collect(Collectors.toList());
  }

  private EducationDto createCertificateEducationDto(Integer date, String specialization) {
    var educationDto = new EducationDto();
    educationDto.setMethod(EducationMethodEnum.CERTIFICATE);
    educationDto.setSpecialization(specialization);
    educationDto.setEndYear(date);
    return educationDto;
  }

  private List<EducationDto> fillSection(
      List<XWPFTableRow> rows, String sectionTitle, EducationMethodEnum method) {
    List<XWPFTableRow> foundedRows = hhConversionUtils.findRowsUpToNextTitle(rows, sectionTitle);
    return fillEducationDtoList(foundedRows, method);
  }

  private List<EducationDto> fillEducationDtoList(
      List<XWPFTableRow> rows, EducationMethodEnum method) {
    return rows.stream()
        .filter(row -> row.getTableCells().size() > 1)
        .map(row -> createEducationDto(row, method))
        .toList();
  }

  private String getEducationType(String level) {
    return Arrays.stream(EducationLevelEnum.values())
        .filter(educationLevel -> level.toLowerCase().contains(educationLevel.getDescription()))
        .findFirst()
        .map(EducationLevelEnum::getValue)
        .orElse(null);
  }

  private EducationDto createEducationDto(XWPFTableRow row, EducationMethodEnum method) {
    EducationDto educationDto = new EducationDto();
    educationDto.setMethod(method);
    setOtherField(educationDto, row.getCell(INFO_CELL_INDEX));
    setEndYear(educationDto, row.getCell(DATE_CELL_INDEX));
    return educationDto;
  }

  private String findUniversityName(XWPFTableCell infoCell) {
    if (infoCell == null) return null;
    return infoCell.getParagraphs().stream()
        .filter(paragraph -> paragraph.getRuns().stream().allMatch(XWPFRun::isBold))
        .map(XWPFParagraph::getText)
        .findFirst()
        .orElse(null);
  }

  private void setOtherField(EducationDto educationDto, XWPFTableCell infoCell) {
    if (infoCell != null) {
      educationDto.setUniversityName(findUniversityName(infoCell));
      if (infoCell.getParagraphs().size() > 1) {
        List<String> facultyAndSpecialization =
            hhConversionUtils.formatFacultyAndSpecialization(
                infoCell.getParagraphs().get(FACULTY_AND_SPECIALIZATION_PARAGRAPH_INDEX).getText());
        if (educationDto.getMethod() == EducationMethodEnum.EDUCATION) {
          educationDto.setFaculty(facultyAndSpecialization.get(FACULTY_INDEX));
        } else {
          educationDto.setOrganization(facultyAndSpecialization.get(FACULTY_INDEX));
        }
        educationDto.setSpecialization(facultyAndSpecialization.get(SPECIALIZATION_INDEX));
      }
    } else log.warn("infoCell is null");
  }

  private void setEndYear(EducationDto educationDto, XWPFTableCell dateCell) {
    if (!dateCell.getText().isBlank()) {
      educationDto.setEndYear(Integer.valueOf(dateCell.getText()));
    }
  }
}
