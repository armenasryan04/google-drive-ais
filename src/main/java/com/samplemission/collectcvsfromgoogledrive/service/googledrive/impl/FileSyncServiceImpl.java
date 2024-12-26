package com.samplemission.collectcvsfromgoogledrive.service.googledrive.impl;

import com.samplemission.collectcvsfromgoogledrive.converter.MultipartFileConverter;
import com.samplemission.collectcvsfromgoogledrive.model.dto.ApplicantDto;
import com.samplemission.collectcvsfromgoogledrive.model.parcer.CvParser;
import com.samplemission.collectcvsfromgoogledrive.service.googledrive.FileSyncService;
import com.samplemission.collectcvsfromgoogledrive.service.googledrive.GoogleDriveService;
import com.samplemission.collectcvsfromgoogledrive.service.googledrive.MinioService;
import com.samplemission.collectcvsfromgoogledrive.service.kafka.KafkaProducerService;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileSyncServiceImpl implements FileSyncService {

  private final GoogleDriveService googleDriveService;
  private final MinioService minioService;
  private final CvParser<ApplicantDto> cvParser;
  private final KafkaProducerService kafkaProducerService;

  /**
   * Выполняет синхронизацию файлов между Google Drive и MinIO.
   *
   * <p>Метод включает следующие шаги: 1. Получение списка файлов из Google Drive и MinIO. 2.
   * Определение файлов для удаления из MinIO. 3. Определение файлов для синхронизации из Google
   * Drive в MinIO. 4. Синхронизация новых или измененных файлов из Google Drive в MinIO. 5. Парсинг
   * синхронизированных файлов в объекты {@link ApplicantDto}. 6. Отправка полученных объектов
   * {@link ApplicantDto} в Kafka. 7. Удаление лишних файлов из MinIO.
   *
   * @throws Exception если возникает ошибка при выполнении синхронизации.
   */
  @Override
  public void syncFiles() throws Exception {
    log.info("синхронизация началaсь!");
    Set<String> googleDriveFiles = googleDriveService.getGoogleDriveFiles();
    Set<String> minioFiles = minioService.getMinioFiles();
    Set<String> filesToDelete = getFilesToDelete(minioFiles);
    Set<String> filesToSync = getFilesToSync(googleDriveFiles, minioFiles);
    syncToMinIO(filesToSync);
    List<ApplicantDto> syncDtos = fetchAndParseApplicantsFromMinio(filesToSync);
    kafkaProducerService.sendApplicantDtoToTopic(syncDtos);
    syncDeleteMinoFiles(filesToDelete);
    log.info("синхронизация завершена!");
  }

  private List<ApplicantDto> fetchAndParseApplicantsFromMinio(Set<String> filesToSync) {
    try {
      return filesToSync.stream()
          .map(
              fileId -> {
                GoogleDriveServiceImpl.FileInfo fileInfo =
                    googleDriveService.getFileWithInputStream(fileId);
                try (InputStream fileStream = fileInfo.getInputStream()) {
                  ApplicantDto applicantDto =
                      cvParser.parseDocument(
                          MultipartFileConverter.convert(
                              fileStream,
                              fileInfo.getFile().getName(),
                              fileInfo.getFile().getMimeType()));
                  applicantDto.setGoogleLink(fileId);
                  return applicantDto;
                } catch (Exception e) {
                  log.trace("Ошибка обработки файла {}: {}", fileId, e.getMessage());
                  return null;
                }
              })
          .filter(dto -> dto != null)
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException("Ошибка при работе с MinIO: " + e.getMessage(), e);
    }
  }

  private void syncToMinIO(Set<String> filesToSync) {
    AtomicInteger syncCount = new AtomicInteger();
    CountDownLatch latch = new CountDownLatch(filesToSync.size());
    filesToSync.parallelStream()
        .forEach(
            fileId -> {
              try (InputStream fileContent = googleDriveService.downloadFile(fileId)) {
                String fileName = googleDriveService.getFileName(fileId);
                minioService.uploadFile(fileName, fileContent);
                syncCount.getAndIncrement();
              } catch (Exception e) {
                log.error("Ошибка при синхронизации файла {}: {}", fileId, e.getMessage(), e);
              } finally {
                latch.countDown();
              }
            });
    try {
      latch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Синхронизация прервана", e);
    }

    log.info("{} файлов из {} синхронизированы в MinIO!", syncCount.get(), filesToSync.size());
  }

  private void syncDeleteMinoFiles(Set<String> filesToDelete) {
    filesToDelete.parallelStream()
        .forEach(
            fileToDelete -> {
              try {
                String fileName = fileToDelete;
                minioService.deleteFile(fileName);
                log.info("Файл {} удален!.", fileName);
              } catch (Exception e) {
                log.trace("Ошибка при удаления файла {}: {}", fileToDelete, e.getMessage());
              }
            });
    log.info("удаление лишних файлов завершен!");
  }

  @NotNull
  private Set<String> getFilesToSync(Set<String> googleDriveFiles, Set<String> minioFiles) {
    Set<String> filesToSync =
        googleDriveFiles.stream()
            .filter(
                fileId -> {
                  try {
                    String fileName = googleDriveService.getFileName(fileId);
                    return !minioFiles.contains(fileName);
                  } catch (IOException e) {
                    log.trace(e.getMessage());
                    return false;
                  }
                })
            .collect(Collectors.toSet());
    return filesToSync;
  }

  @NotNull
  private Set<String> getFilesToDelete(Set<String> minioFiles) {
    Set<String> filesToDelete =
        minioFiles.stream()
            .filter(
                fileName -> {
                  try {
                    String fileId = googleDriveService.getFileIdByName(fileName);
                    return fileId == null;
                  } catch (IOException e) {
                    log.trace(e.getMessage());
                    return false;
                  }
                })
            .collect(Collectors.toSet());
    return filesToDelete;
  }
}
