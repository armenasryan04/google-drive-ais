package com.samplemission.collectcvsfromgoogledrive.model.parcer.v2;

import com.samplemission.collectcvsfromgoogledrive.model.dto.ApplicantDto;
import com.samplemission.collectcvsfromgoogledrive.model.dto.LanguageDto;
import com.samplemission.collectcvsfromgoogledrive.model.dto.SkillDto;
import com.samplemission.collectcvsfromgoogledrive.model.parcer.DirectoryData;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KeySkillsInfoParser {

  public static final int MAIN_INFO_CELL_INDEX = 1;
  public static final int TITLE_CELL_INDEX = 0;

  public void fillKeySkillsInfo(ApplicantDto applicantDto, List<XWPFTableRow> rows) {
    fillLanguageInfoIfPresent(applicantDto, rows);
    fillSkillsInfoIfPresent(applicantDto, rows);
  }

  private void fillLanguageInfoIfPresent(ApplicantDto applicantDto, List<XWPFTableRow> rows) {
    Optional<XWPFTableRow> languageRow = findRowByCellValue(rows, "Знание языков");
    languageRow.ifPresentOrElse(
        lan -> {
          List<XWPFParagraph> languageParagraphs =
              lan.getCell(MAIN_INFO_CELL_INDEX).getParagraphs();
          fillLanguageInfo(applicantDto, languageParagraphs);
        },
        () -> log.info("Language info for applicant is empty"));
  }

  private void fillSkillsInfoIfPresent(ApplicantDto applicantDto, List<XWPFTableRow> rows) {
    Optional<XWPFTableRow> skillsRow = findRowByCellValue(rows, "Навыки");
    skillsRow.ifPresentOrElse(
        skill -> {
          List<XWPFParagraph> skillsParagraphs =
              skill.getCell(MAIN_INFO_CELL_INDEX).getParagraphs();
          fillSkillsInfo(applicantDto, skillsParagraphs);
        },
        () -> log.info("Skills info for applicant is empty"));
  }

  private Optional<XWPFTableRow> findRowByCellValue(List<XWPFTableRow> rows, String cellValue) {
    return rows.stream()
        .filter(row -> row.getCell(TITLE_CELL_INDEX).getText().equals(cellValue))
        .findFirst();
  }

  public void fillLanguageInfo(ApplicantDto applicantDto, List<XWPFParagraph> paragraphs) {
    List<LanguageDto> languageDtos =
        paragraphs.stream()
            .flatMap(
                paragraph ->
                    DirectoryData.LANGUAGE.entrySet().stream()
                        .flatMap(
                            language ->
                                DirectoryData.LANGUAGE_LEVEL.entrySet().stream()
                                    .filter(
                                        languageLvl ->
                                            paragraph
                                                .getText()
                                                .matches(
                                                    language.getKey()
                                                        + ".+"
                                                        + languageLvl.getKey()))
                                    .map(
                                        languageLvl ->
                                            new LanguageDto(
                                                language.getValue(),
                                                languageLvl.getValue(),
                                                languageLvl.getKey()))))
            .toList();
    applicantDto.setLanguages(languageDtos);
  }

  public void fillSkillsInfo(ApplicantDto applicantDto, List<XWPFParagraph> paragraphs) {
    List<SkillDto> applicantSkillDtoList =
        DirectoryData.SKILL.entrySet().stream()
            .flatMap(
                skill ->
                    paragraphs.stream()
                        .filter(paragraph -> paragraph.getText().contains(skill.getKey()))
                        .map(paragraph -> new SkillDto(skill.getValue())))
            .toList();
    applicantDto.setSkills(applicantSkillDtoList);
  }
}
