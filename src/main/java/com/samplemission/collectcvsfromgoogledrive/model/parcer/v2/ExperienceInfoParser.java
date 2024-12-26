package com.samplemission.collectcvsfromgoogledrive.model.parcer.v2;

import com.samplemission.collectcvsfromgoogledrive.model.dto.ApplicantDto;
import com.samplemission.collectcvsfromgoogledrive.model.dto.ExperienceActivityDto;
import com.samplemission.collectcvsfromgoogledrive.model.dto.ExperienceDto;
import com.samplemission.collectcvsfromgoogledrive.utill.HhConversionUtils;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExperienceInfoParser {
  public String startDateRegex = "[а-яА-Я]+ \\d+(?= —)";
  public String endDateRegex = "(?<=— )[а-яА-Я]+ \\d+";
  public static final int DATE_CELL_INDEX = 0;
  public static final int MAIN_INFO_CELL_INDEX = 2;
  public static final int FIRST_DAY_OF_MONTH = 1;
  public static final int ORGANIZATION_NAME_INDEX = 0;
  public static final int POSITION_INDEX = 1;
  public static final double FONT_SIZE_PLAIN_TEXT = 9.0;
  private static final int CITY_INDEX = 0;
  private static final int SITE_INDEX = 1;
  private static final int FIRST_LETTER_INDEX = 0;

  private final DateTimeFormatter monthYearPatternRu;
  private final HhConversionUtils hhConversionUtils;

  public void fillExperienceInfo(ApplicantDto applicantDto, List<XWPFTableRow> experienceRows) {
    List<ExperienceDto> applicantExperienceDtos =
        experienceRows.stream()
            .filter(row -> row.getTableCells().size() > 1)
            .map(
                row -> {
                  String dateTexts =
                      row.getCell(DATE_CELL_INDEX).getParagraphs().stream()
                          .map(XWPFParagraph::getText)
                          .collect(Collectors.joining());
                  List<XWPFParagraph> mainInfoParagraphs =
                      row.getCell(MAIN_INFO_CELL_INDEX).getParagraphs();
                  ExperienceDto applicantExperienceDto = new ExperienceDto();
                  fillExperienceDates(applicantExperienceDto, dateTexts);
                  fillExperienceMainInfo(applicantExperienceDto, mainInfoParagraphs);
                  return applicantExperienceDto;
                })
            .toList();
    applicantDto.setExperiences(applicantExperienceDtos);
  }

  public void fillExperienceDates(ExperienceDto applicantExperienceDto, String text) {
    applicantExperienceDto.setStartDate(getDateByRegex(text, startDateRegex));
    applicantExperienceDto.setEndDate(getDateByRegex(text, endDateRegex));
  }

  private LocalDate getDateByRegex(String text, String regex) {
    String dateTime = hhConversionUtils.findByRegex(regex, text);
    if (dateTime != null) {
      YearMonth dateYearMonth = YearMonth.parse(dateTime.toLowerCase(), monthYearPatternRu);
      return dateYearMonth.atDay(FIRST_DAY_OF_MONTH);
    }
    return null;
  }

  @SneakyThrows
  public void fillExperienceMainInfo(
      ExperienceDto applicantExperienceDto, List<XWPFParagraph> mainInfoParagraphs) {
    List<XWPFParagraph> nameAndPositionParagraphs =
        getNameAndPositionParagraphs(mainInfoParagraphs);
    XWPFParagraph organisationName = nameAndPositionParagraphs.get(ORGANIZATION_NAME_INDEX);
    XWPFParagraph position = nameAndPositionParagraphs.get(POSITION_INDEX);
    applicantExperienceDto.setCompanyName(organisationName.getText());
    applicantExperienceDto.setSite(getSite(mainInfoParagraphs));
    applicantExperienceDto.setPosition(position.getText());
    applicantExperienceDto.setDescription(
        getDescription(mainInfoParagraphs, organisationName, position));
    applicantExperienceDto.setActivity(
        createExperienceActivityDto(
            position.getText(), getActivities(mainInfoParagraphs, position)));
  }

  private String getSite(List<XWPFParagraph> mainInfoParagraphs) {
    String cityAndSite = getCityAndSite(mainInfoParagraphs);
    if (cityAndSite != null) {
      String[] parts = cityAndSite.split(", ");
      if (parts.length == 2) {
        return parts[SITE_INDEX];
      }
      if (parts.length == 1
          && Character.isLowerCase(parts[CITY_INDEX].charAt(FIRST_LETTER_INDEX))) {
        return parts[CITY_INDEX];
      }
    }
    return null;
  }

  private String getCityAndSite(List<XWPFParagraph> mainInfoParagraphs) {
    return mainInfoParagraphs.stream()
        .filter(
            xwpfParagraph ->
                xwpfParagraph.getRuns().stream()
                    .anyMatch(run -> Objects.equals(run.getColor(), "AEAEAE")))
        .findFirst()
        .map(XWPFParagraph::getText)
        .orElse(null);
  }

  private List<XWPFParagraph> getNameAndPositionParagraphs(List<XWPFParagraph> mainInfoParagraphs) {
    return mainInfoParagraphs.stream()
        .filter(
            xwpfParagraph ->
                xwpfParagraph.getRuns().stream()
                    .anyMatch(
                        run ->
                            run.getFontSizeAsDouble() == null
                                || run.getFontSizeAsDouble() != FONT_SIZE_PLAIN_TEXT))
        .toList();
  }

  private List<String> getActivities(
      List<XWPFParagraph> mainInfoParagraphs, XWPFParagraph position) {
    List<XWPFParagraph> activityParagraphs =
        hhConversionUtils.findParagraphsBetween(mainInfoParagraphs, position, null);
    return activityParagraphs.stream()
        .map(XWPFParagraph::getText)
        .map(text -> text.replace("\n", "\n\n"))
        .toList();
  }

  private String getDescription(
      List<XWPFParagraph> mainInfoParagraphs,
      XWPFParagraph organisationName,
      XWPFParagraph position) {
    List<XWPFParagraph> descriptionParagraphs =
        hhConversionUtils.findParagraphsFromTo(mainInfoParagraphs, organisationName, position);
    return descriptionParagraphs.stream()
        .filter(paragraph -> !Objects.equals(paragraph, organisationName))
        .filter(
            paragraph ->
                paragraph.getRuns().stream()
                    .noneMatch(run -> Objects.equals(run.getColor(), "AEAEAE")))
        .map(XWPFParagraph::getText)
        .map(text -> text.replace("•", "—"))
        .collect(Collectors.joining("\n\n"));
  }

  private ExperienceActivityDto createExperienceActivityDto(
      String activityName, List<String> activities) {
    ExperienceActivityDto experienceActivityDto = new ExperienceActivityDto();
    experienceActivityDto.setActivityName(activityName);
    experienceActivityDto.setDetails(activities);
    return experienceActivityDto;
  }
}
