package com.samplemission.collectcvsfromgoogledrive.model.parcer.v2;

import com.samplemission.collectcvsfromgoogledrive.model.dto.ApplicantDto;
import com.samplemission.collectcvsfromgoogledrive.model.dto.ApplicantScheduleDto;
import com.samplemission.collectcvsfromgoogledrive.model.dto.EmploymentDto;
import com.samplemission.collectcvsfromgoogledrive.model.enums.ScheduleEnum;
import com.samplemission.collectcvsfromgoogledrive.model.parcer.DirectoryData;
import com.samplemission.collectcvsfromgoogledrive.utill.HhConversionUtils;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PositionInfoParser {

  public String positionRegex = ".+(?=\\nСпециализации)";
  public String currencySalaryRegex = "(\\d+\\n|(\\d+ \\d+))\\n.+";
  public static final int POSITION_ROW_INDEX = 0;
  public static final int POSITION_CELL_INDEX = 0;
  public static final int SALARY_CELL_INDEX = 1;
  public static final int ROW_SIZE_WITH_SALARY = 2;

  private final HhConversionUtils hhConversionUtils;

  public void fillPositionInfo(ApplicantDto applicantDto, List<XWPFTableRow> positionRows) {
    XWPFTableRow positionRow = positionRows.get(POSITION_ROW_INDEX);
    fillPosition(
        applicantDto,
        positionRow.getCell(POSITION_CELL_INDEX).getParagraphs().stream()
            .map(XWPFParagraph::getText)
            .collect(Collectors.joining("\n")));
    fillEmployments(applicantDto, positionRow.getCell(POSITION_CELL_INDEX).getParagraphs());
    fillSchedule(applicantDto, positionRow.getCell(POSITION_CELL_INDEX).getParagraphs());
    if (positionRow.getTableCells().size() == ROW_SIZE_WITH_SALARY) {
      fillSalaryAndCurrency(
          applicantDto,
          positionRow.getCell(SALARY_CELL_INDEX).getParagraphs().stream()
              .map(XWPFParagraph::getText)
              .collect(Collectors.joining("\n")));
    }
  }

  public void fillPosition(ApplicantDto applicantDto, String text) {
    String position = hhConversionUtils.findByRegex(positionRegex, text);
    applicantDto.setPosition(position);
    applicantDto.setGrade(null);
  }

  private void fillEmployments(ApplicantDto applicantDto, List<XWPFParagraph> paragraphs) {
    Optional<XWPFParagraph> employmentParagraph =
        paragraphs.stream()
            .filter(paragraph -> paragraph.getText().startsWith("Занятость:"))
            .findFirst();

    employmentParagraph.ifPresentOrElse(
        emp ->
            applicantDto.setEmployments(
                DirectoryData.EMPLOYMENT_TYPE.entrySet().stream()
                    .filter(
                        entry -> emp.getText().toLowerCase().contains(entry.getKey().toLowerCase()))
                    .map(entry -> new EmploymentDto(entry.getValue()))
                    .toList()),
        () -> log.info("Employments info for applicant is empty"));
  }

  private void fillSchedule(ApplicantDto applicantDto, List<XWPFParagraph> paragraphs) {
    List<ScheduleEnum> scheduleEnumList = Arrays.stream(ScheduleEnum.values()).toList();
    Optional<XWPFParagraph> scheduleParagraph =
        paragraphs.stream()
            .filter(paragraph -> paragraph.getText().startsWith("График работы:"))
            .findFirst();
    scheduleParagraph.ifPresentOrElse(
        (sch) ->
            applicantDto.setSchedules(
                scheduleEnumList.stream()
                    .filter(
                        scheduleEnum ->
                            sch.getText().contains(scheduleEnum.getName().toLowerCase()))
                    .map(ApplicantScheduleDto::new)
                    .toList()),
        () -> log.info("Schedule info for applicant is empty"));
  }

  public void fillSalaryAndCurrency(ApplicantDto applicantDto, String text) {
    String salaryCurrency = hhConversionUtils.findByRegex(currencySalaryRegex, text);
    if (salaryCurrency == null) {
      return;
    }
    String salary = hhConversionUtils.findByRegex("(\\d+\\n|(\\d+ \\d+))", salaryCurrency);
    String currency = hhConversionUtils.findByRegex("(?<=\\n).+", salaryCurrency);
    salary = salary.replace(" ", "");

    applicantDto.setCurrency(DirectoryData.CURRENCY.getOrDefault(currency, null));
    applicantDto.setSalary(new BigInteger(salary));
  }
}
