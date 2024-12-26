package com.samplemission.collectcvsfromgoogledrive.model.parcer;

import com.samplemission.collectcvsfromgoogledrive.model.dto.*;
import com.samplemission.collectcvsfromgoogledrive.model.enums.BusinessTripTypeEnum;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ResumeWriter {

  public void writeToDocx(ApplicantDto applicantDto, String filePath) throws IOException {
    try (XWPFDocument document = new XWPFDocument()) {

      // Устанавливаем отступы страницы
      setDocumentMargins(document);

      // Формируем документ по секциям
      addHeaderSection(document, applicantDto);
      addContactSection(document, applicantDto);
      addStyledSection(
          document, "Желаемая должность и зарплата", formatPositionAndSalary(applicantDto));
      addStyledSection(document, "Опыт работы", formatExperience(applicantDto));
      addStyledSection(document, "Образование", formatEducation(applicantDto));
      addStyledSection(document, "Ключевые навыки", formatSkills(applicantDto));
      addFooterSection(document);

      // Сохраняем документ
      saveDocument(document, filePath);
    }
  }

  private void setDocumentMargins(XWPFDocument document) {
    CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
    CTPageMar pageMar = sectPr.addNewPgMar();
    pageMar.setLeft(1440); // 2 см слева
    pageMar.setRight(1440); // 2 см справа
    pageMar.setTop(1440); // 2 см сверху
    pageMar.setBottom(1440); // 2 см снизу
  }

  private void addHeaderSection(XWPFDocument document, ApplicantDto dto) {
    XWPFParagraph header = document.createParagraph();
    header.setAlignment(ParagraphAlignment.LEFT);
    XWPFRun run = header.createRun();
    run.setText(dto.getSurname() + " " + dto.getName() + " " + dto.getPatronym());
    run.setBold(true);
    run.setFontSize(16);
    run.setFontFamily("Arial");
    run.addBreak();
    run.setText(
        dto.getGender()
            + ", "
            + (dto.getDateBirth() != null
                ? dto.getDateBirth()
                    .format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ru")))
                : "Дата рождения не указана"));
    run.setFontSize(12);
    run.addBreak();
    run.setText("Проживает: " + dto.getCity() + ", " + dto.getCountry());
    run.addBreak();
    run.setText("Гражданство: " + dto.getNationality());
    run.addBreak();
    // Определение типа командировки
    String businessTripInfo = determineBusinessTripType(dto.getIsBusinessTrip().getDescription());
    run.setText(businessTripInfo);
  }

  private String determineBusinessTripType(String description) {
    for (BusinessTripTypeEnum type : BusinessTripTypeEnum.values()) {
      if (description != null && description.matches(type.getDescription())) {
        return type.getCorrectDescription();
      }
    }
    return "Информация о готовности к командировкам отсутствует.";
  }

  private void addContactSection(XWPFDocument document, ApplicantDto dto) {
    XWPFParagraph contact = document.createParagraph();
    contact.setAlignment(ParagraphAlignment.LEFT);
    XWPFRun run = contact.createRun();

    if (dto.getContacts() != null && !dto.getContacts().isEmpty()) {
      for (ContactDto contactInfo : dto.getContacts()) {
        run.setText(contactInfo.getContactType() + ": " + contactInfo.getValue());
        run.addBreak();
      }
    } else {
      run.setText("Контактная информация отсутствует.");
    }

    run.setFontSize(12);
    run.setFontFamily("Arial");
    contact.setSpacingAfter(200);
  }

  private void addStyledSection(XWPFDocument document, String title, String content) {
    XWPFParagraph titleParagraph = document.createParagraph();
    titleParagraph.setAlignment(ParagraphAlignment.LEFT);
    XWPFRun titleRun = titleParagraph.createRun();
    titleRun.setText(title);
    titleRun.setBold(true);
    titleRun.setFontSize(14);
    titleRun.setFontFamily("Arial");
    titleParagraph.setSpacingAfter(100);

    XWPFParagraph contentParagraph = document.createParagraph();
    contentParagraph.setAlignment(ParagraphAlignment.LEFT);
    contentParagraph.setSpacingBetween(1.5);
    XWPFRun contentRun = contentParagraph.createRun();
    contentRun.setText(content);
    contentRun.setFontSize(12);
    contentRun.setFontFamily("Arial");
    contentRun.setBold(false);
    contentParagraph.setSpacingAfter(200);
  }

  private void addFooterSection(XWPFDocument document) {
    XWPFParagraph footer = document.createParagraph();
    footer.setAlignment(ParagraphAlignment.LEFT);
    XWPFRun run = footer.createRun();
    run.setText(
        "Резюме обновлено " + LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy")));
    run.setFontSize(10);
    run.setFontFamily("Arial");
  }

  private String formatPositionAndSalary(ApplicantDto dto) {
    String position = dto.getPosition() != null ? dto.getPosition() : "Должность не указана";
    String salary =
        dto.getSalary() != null
            ? String.format("%,d", dto.getSalary()).replace(',', ' ') + " " + dto.getCurrency()
            : "Зарплата не указана";
    return position + "\n" + salary;
  }

  private String formatExperience(ApplicantDto dto) {
    if (dto.getExperiences() == null || dto.getExperiences().isEmpty()) {
      return "Нет опыта работы.";
    }
    StringBuilder builder = new StringBuilder();
    for (ExperienceDto experience : dto.getExperiences()) {
      builder
          .append(experience.getCompanyName())
          .append("\nПериод: ")
          .append(experience.getStartDate())
          .append(" - ")
          .append(experience.getEndDate() != null ? experience.getEndDate() : "текущий момент")
          .append("\nДолжность: ")
          .append(experience.getPosition())
          .append("\nОписание: ")
          .append(experience.getDescription())
          .append("\n\n");
    }
    return builder.toString().trim();
  }

  private String formatEducation(ApplicantDto dto) {
    if (dto.getEducation() == null || dto.getEducation().isEmpty()) {
      return "Нет информации об образовании.";
    }
    StringBuilder builder = new StringBuilder();
    for (EducationDto education : dto.getEducation()) {
      builder
          .append(education.getUniversityName())
          .append("\nСпециализация: ")
          .append(education.getSpecialization())
          .append("\nГод окончания: ")
          .append(education.getEndYear())
          .append("\n\n");
    }
    return builder.toString().trim();
  }

  private String formatSkills(ApplicantDto dto) {
    if (dto.getSkills() == null || dto.getSkills().isEmpty()) {
      return "Нет данных о ключевых навыках.";
    }
    return dto.getSkills().stream().map(SkillDto::getSkillName).collect(Collectors.joining("\n"));
  }

  private void saveDocument(XWPFDocument document, String filePath) throws IOException {
    File file = new File(filePath);
    if (!file.getParentFile().exists()) {
      if (!file.getParentFile().mkdirs()) {
        throw new IOException("Не удалось создать директорию для файла: " + filePath);
      }
    }
    try (FileOutputStream out = new FileOutputStream(file)) {
      document.write(out);
    }
  }
}
