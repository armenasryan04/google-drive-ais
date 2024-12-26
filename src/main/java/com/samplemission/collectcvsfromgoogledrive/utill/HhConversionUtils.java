package com.samplemission.collectcvsfromgoogledrive.utill;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;

@Component
public class HhConversionUtils {
  public String findByRegex(String regex, String text) {
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(text);
    return matcher.find() ? matcher.group() : null;
  }

  public Boolean checkIfContainsRegex(String regex, String text) {
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(text);
    return matcher.find();
  }

  public XWPFTableRow findNextTitleRow(List<XWPFTableRow> rows, XWPFTableRow startRow) {
    return rows.stream()
        .filter(
            row -> rows.indexOf(row) > rows.indexOf(startRow) && row.getTableCells().size() == 1)
        .filter(
            row ->
                row.getCell(0).getParagraphs().stream()
                    .noneMatch(
                        paragraph ->
                            paragraph.getRuns().stream()
                                .noneMatch(run -> Objects.equals(run.getColor(), "AEAEAE"))))
        .findFirst()
        .orElse(null);
  }

  public Optional<XWPFTableRow> findTitleRow(List<XWPFTableRow> rows, String title) {
    return rows.stream()
        .filter(
            row -> row.getTableCells().stream().anyMatch(cell -> cell.getText().contains(title)))
        .findFirst();
  }

  public List<XWPFParagraph> findParagraphsFromTo(
      List<XWPFParagraph> paragraphs, XWPFParagraph start, XWPFParagraph finish) {
    if (finish == null) {
      return paragraphs.subList(paragraphs.indexOf(start), paragraphs.size());
    }
    return paragraphs.subList(paragraphs.indexOf(start), paragraphs.indexOf(finish));
  }

  public List<XWPFParagraph> findParagraphsBetween(
      List<XWPFParagraph> paragraphs, XWPFParagraph start, XWPFParagraph finish) {
    int startIndex = paragraphs.indexOf(start) + 1;
    if (finish == null) {
      return paragraphs.subList(startIndex, paragraphs.size());
    }
    return paragraphs.subList(startIndex, paragraphs.indexOf(finish));
  }

  public List<XWPFTableRow> findRowsFromToTitleRows(
      List<XWPFTableRow> rows, XWPFTableRow start, XWPFTableRow finish) {
    if (finish == null) {
      return rows.subList(rows.indexOf(start), rows.size());
    }
    return rows.subList(rows.indexOf(start), rows.indexOf(finish));
  }

  public List<XWPFTableRow> findRowsBetween(
      List<XWPFTableRow> rows, XWPFTableRow start, XWPFTableRow finish) {
    int startIndex = rows.indexOf(start) + 1;
    if (finish == null) {
      return rows.subList(startIndex, rows.size());
    }
    return rows.subList(startIndex, rows.indexOf(finish));
  }

  public List<XWPFTableRow> findRowsUpToNextTitle(List<XWPFTableRow> rows, String start) {
    Optional<XWPFTableRow> titleRow = findTitleRow(rows, start);
    if (titleRow.isEmpty()) {
      return new ArrayList<>();
    }
    XWPFTableRow nextTitleRow = findNextTitleRow(rows, titleRow.get());
    return findRowsBetween(rows, titleRow.get(), nextTitleRow);
  }

  public List<String> formatFacultyAndSpecialization(String raw) {
    List<String> processed = new ArrayList<>(Arrays.asList(raw.split(",", 2)));
    if (processed.size() == 1) {
      processed.add("Значение отсутствует");
    }
    return processed;
  }

  public boolean getNonGreyText(XWPFTableRow row) {
    return row.getCell(0).getParagraphs().stream()
        .anyMatch(
            paragraph ->
                paragraph.getRuns().stream()
                    .noneMatch(run -> Objects.equals(run.getColor(), "AEAEAE")));
  }
}
