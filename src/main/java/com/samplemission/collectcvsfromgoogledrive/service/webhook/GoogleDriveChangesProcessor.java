package com.samplemission.collectcvsfromgoogledrive.service.webhook;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.ChangeList;
import com.google.api.services.drive.model.Channel;
import com.google.api.services.drive.model.File;
import com.samplemission.collectcvsfromgoogledrive.converter.MultipartFileConverter;
import com.samplemission.collectcvsfromgoogledrive.model.dto.ApplicantDto;
import com.samplemission.collectcvsfromgoogledrive.model.parcer.CvParser;
import com.samplemission.collectcvsfromgoogledrive.service.googledrive.GoogleDriveService;
import com.samplemission.collectcvsfromgoogledrive.service.googledrive.MinioService;
import com.samplemission.collectcvsfromgoogledrive.service.kafka.KafkaProducerService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleDriveChangesProcessor {

  private final Drive driveClient;
  private final MinioService minioService;
  private final KafkaProducerService kafkaProducerService;
  private final GoogleDriveService googleDriveService;
  private final CvParser<ApplicantDto> cvParser;
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private static final Set<String> md5NonTrashedFileSet = ConcurrentHashMap.newKeySet();
  private static final Set<String> md5TrashedFileSet = ConcurrentHashMap.newKeySet();
  private static final long FILE_EXPIRATION_DELAY_MINUTES = 30;

  private static final String FILE_FIELDS =
      "id, kind, mimeType, name, trashed, md5Checksum, parents";
  public static final String FILE_TYPE = "file";

  @Value("${google.drive.folder.id}")
  private String folderId;

  private String savedPageToken;
  private String savedChannelId;

  public void updateChannelData(String newPageToken, String newChannelId, String resourceId) {
    String oldChannelId = savedChannelId;
    this.savedPageToken = newPageToken;
    this.savedChannelId = newChannelId;
    stopWebhookChannel(oldChannelId, resourceId);
  }

  /**
   * Обрабатывает изменения файлов в Google Drive.
   *
   * @param channelId Идентификатор канала, с которого пришло уведомление.
   * @param resourceId Идентификатор измененного ресурса в Google Drive.
   */
  public void processChanges(String channelId, String resourceId) {
    if (channelId.equals(savedChannelId)) {
      try {
        String pageToken = savedPageToken;

        while (pageToken != null) {
          ChangeList changes =
              driveClient
                  .changes()
                  .list(pageToken)
                  .setIncludeRemoved(false)
                  .setIncludeCorpusRemovals(false)
                  .execute();
          processFileChanges(changes);

          if (changes.getNewStartPageToken() != null)
            savedPageToken = changes.getNewStartPageToken();

          pageToken = changes.getNextPageToken();
        }
      } catch (IOException e) {
        log.error("An error occurred while fetching changes from Google Drive: {}", e.getMessage());
      }
    } else {
      stopWebhookChannel(channelId, resourceId);
    }
  }

  /**
   * Обрабатывает список изменений файлов.
   *
   * @param changes список изменений файлов из Google Drive.
   */
  private void processFileChanges(ChangeList changes) {
    for (Change change : changes.getChanges()) {
      if (change.getFile() != null && change.getChangeType().equals(FILE_TYPE)) {
        String fileId = change.getFileId();
        processFileChange(fileId);
      }
    }
  }

  /**
   * Обрабатывает изменение конкретного файла по его идентификатору.
   *
   * @param fileId идентификатор файла в Google Drive.
   */
  private void processFileChange(String fileId) {
    String checksum = null;
    try {
      File file = driveClient.files().get(fileId).setFields(FILE_FIELDS).execute();
      if (isChangeInFolder(file)) {
        checksum = file.getMd5Checksum();
        if (Boolean.TRUE.equals(file.getTrashed())) {
          handleTrashedFile(file);
        } else {
          handleNonTrashedFile(file);
        }
      }
    } catch (IOException e) {
      log.error("File with ID {} not found in Google Drive: {}", fileId, e.getMessage());
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      scheduleFileMd5Removal(checksum);
    }
  }

  /**
   * Проверяет, относится ли изменение к отслеживаемой папке Google Drive.
   *
   * @param file файл, который был изменен в Google Drive.
   * @return {@code true}, если файл принадлежит отслеживаемой папке, иначе {@code false}.
   */
  private boolean isChangeInFolder(File file) {
    return file != null && file.getParents() != null && file.getParents().contains(folderId);
  }

  /**
   * Обрабатывает файл, который не находится в корзине Google Drive.
   *
   * <p>Метод выполняет следующие шаги: 1. Проверяет, был ли файл уже обработан (по его MD5-хэшу).
   * 2. Загружает файл из Google Drive и сохраняет его в MinIO. 3. Конвертирует файл в формат {@link
   * MultipartFile}. 4. Парсит содержимое файла в объект {@link ApplicantDto}. 5. Устанавливает
   * ссылку Google Drive в {@link ApplicantDto}. 6. Отправляет полученный объект {@link
   * ApplicantDto} в Kafka.
   *
   * @param file файл из Google Drive для обработки.
   * @throws Exception если возникает ошибка при обработке файла.
   */
  private void handleNonTrashedFile(File file) throws Exception {
    if (!md5NonTrashedFileSet.add(file.getMd5Checksum())) return;
    log.info(
        "Google Drive File Added - ID: {}, Name: {}, Type: {}",
        file.getId(),
        file.getName(),
        file.getMimeType());
    InputStream fileInputstream = googleDriveService.downloadFileAsStream(file);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    fileInputstream.transferTo(baos);
    InputStream streamForMinio = new ByteArrayInputStream(baos.toByteArray());
    InputStream streamForConverter = new ByteArrayInputStream(baos.toByteArray());
    minioService.uploadFile(file.getName(), streamForMinio);
    MultipartFile multipartFile =
        MultipartFileConverter.convert(streamForConverter, file.getName(), file.getMimeType());
    ApplicantDto applicantDto = cvParser.parseDocument(multipartFile);
    applicantDto.setGoogleLink(file.getId());
    kafkaProducerService.sendApplicantDtoToTopic(applicantDto);
    log.info("файл обработан успешно!");
  }

  /**
   * Обрабатывает файл, который был перемещен в корзину.
   *
   * @param file файл, который был удален.
   */
  private void handleTrashedFile(File file) {
    if (!md5TrashedFileSet.add(file.getMd5Checksum())) return;
    log.info(
        "Google Drive File Moved to Trash - ID: {}, Name: {}, Type: {}",
        file.getId(),
        file.getName(),
        file.getMimeType());
    minioService.deleteFile(file.getName());
    log.info("файл удален из минио!");
  }

  /**
   * Планирует удаление контрольной суммы файла из соответствующих наборов.
   *
   * @param checksum контрольная сумма файла, которую необходимо удалить из наборов.
   */
  private void scheduleFileMd5Removal(String checksum) {
    if (checksum != null) {
      scheduleRemoval(md5NonTrashedFileSet, checksum);
      scheduleRemoval(md5TrashedFileSet, checksum);
    }
  }

  /**
   * Планирует удаление хэша MD5 из указанного множества через заданное время.
   *
   * @param set множество, из которого будет удален хэш MD5.
   * @param checksum MD5-хэш файла.
   */
  private void scheduleRemoval(Set<String> set, String checksum) {
    if (checksum != null && set.contains(checksum)) {
      scheduler.schedule(
          () -> {
            set.remove(checksum);
            log.info("MD5 hash '{}' has been removed from the set after expiration.", checksum);
          },
          FILE_EXPIRATION_DELAY_MINUTES,
          TimeUnit.MINUTES);
    }
  }

  /** Завершает работу планировщика при остановке приложения. */
  @PreDestroy
  public void preDestroy() {
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Завершает неактуальный канал уведомлений Webhook.
   *
   * @param channelId идентификатор канала.
   * @param resourceId идентификатор ресурса.
   */
  public void stopWebhookChannel(String channelId, String resourceId) {
    if (channelId != null && resourceId != null) {
      try {
        Channel channel = new Channel();
        channel.setId(channelId);
        channel.setResourceId(resourceId);
        driveClient.channels().stop(channel).execute();
        log.info("Stopped Webhook Channel - ID: {}, ResourceId: {}", channelId, resourceId);
      } catch (IOException e) {
        log.error("An error occurred while stopping webhook channel: {}", e.getMessage());
      }
    }
  }
}
