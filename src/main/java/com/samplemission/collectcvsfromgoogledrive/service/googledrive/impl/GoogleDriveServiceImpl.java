package com.samplemission.collectcvsfromgoogledrive.service.googledrive.impl;

import com.google.api.client.http.HttpResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.samplemission.collectcvsfromgoogledrive.exceptions.GoogleDriveOperationException;
import com.samplemission.collectcvsfromgoogledrive.service.googledrive.GoogleDriveService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleDriveServiceImpl implements GoogleDriveService {
  @Value("${google.drive.folder.id}")
  private String folderId;

  private final Drive driveService;

  public Set<String> getGoogleDriveFiles() throws IOException {

    FileList result =
        driveService
            .files()
            .list()
            .setQ(String.format("'%s' in parents and trashed = false", folderId))
            .setPageSize(1000)
            .setFields("files(id, name)")
            .execute();
    return result.getFiles().stream().map(File::getId).collect(Collectors.toSet());
  }

  public InputStream downloadFile(String fileId) throws IOException {
    return driveService.files().get(fileId).executeMediaAsInputStream();
  }

  public String getFileName(String fileId) throws IOException {
    return driveService.files().get(fileId).setFields("name").execute().getName();
  }

  public InputStream downloadFileAsStream(File file) throws GoogleDriveOperationException {
    String fileId = file.getId();
    try (InputStream inputStream = driveService.files().get(fileId).executeMediaAsInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[4096];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
      return new ByteArrayInputStream(outputStream.toByteArray());
    } catch (HttpResponseException e) {
      throw new GoogleDriveOperationException(
          "HTTP error while executing request to Google Drive API: " + e.getContent());
    } catch (IOException e) {
      throw new GoogleDriveOperationException(
          "Error reading file content from Google Drive: " + e.getMessage());
    }
  }

  public String getFileIdByName(String fileName) throws IOException {
    FileList result =
        driveService
            .files()
            .list()
            .setQ(String.format("name='%s' and trashed=false", fileName))
            .setFields("files(id, name)")
            .execute();
    return result.getFiles().stream().findFirst().map(File::getId).orElse(null);
  }

  public FileInfo getFileWithInputStream(String fileId) {
    File file = null;
    try {
      file = driveService.files().get(fileId).setFields("id, name, mimeType").execute();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    InputStream fileInputStream = null;
    try {
      fileInputStream = driveService.files().get(fileId).executeMediaAsInputStream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return new FileInfo(file, fileInputStream);
  }

  /** Класс для представления информации о файле: метаданные и InputStream. */
  public static class FileInfo {
    private File file;
    private InputStream inputStream;

    public FileInfo(File file, InputStream inputStream) {
      this.file = file;
      this.inputStream = inputStream;
    }

    public File getFile() {
      return file;
    }

    public InputStream getInputStream() {
      return inputStream;
    }
  }
}
