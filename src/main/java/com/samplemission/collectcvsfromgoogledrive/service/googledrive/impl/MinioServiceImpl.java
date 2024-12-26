package com.samplemission.collectcvsfromgoogledrive.service.googledrive.impl;

import com.samplemission.collectcvsfromgoogledrive.config.MinioCredentialProperties;
import com.samplemission.collectcvsfromgoogledrive.service.googledrive.MinioService;
import io.minio.*;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MinioServiceImpl implements MinioService {

  private final MinioClient minioClient;
  private final String bucketName;

  public MinioServiceImpl(MinioCredentialProperties properties) {
    this.minioClient =
        MinioClient.builder()
            .endpoint(properties.getEndpoint())
            .credentials(properties.getAccessKey(), properties.getSecretKey())
            .build();
    this.bucketName = properties.getBucketName();
  }

  @Override
  public Set<String> getMinioFiles() throws Exception {
    try {
      isBucketExists();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return StreamSupport.stream(
            minioClient
                .listObjects(ListObjectsArgs.builder().bucket(bucketName).build())
                .spliterator(),
            false)
        .map(
            itemResult -> {
              try {
                return itemResult.get().objectName();
              } catch (Exception e) {
                throw new RuntimeException("Error retrieving item from MinIO", e);
              }
            })
        .collect(Collectors.toSet());
  }

  @Override
  public void uploadFile(String fileName, InputStream inputStream) throws Exception {
    try {
      isBucketExists();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    minioClient.putObject(
        PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                inputStream, -1, PutObjectArgs.MIN_MULTIPART_SIZE)
            .build());
  }

  @Override
  public void deleteFile(String fileName) {
    try {
      isBucketExists();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    try {
      minioClient.removeObject(
          RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
      log.info("Файл {} успешно удален из MinIO.", fileName);
    } catch (Exception e) {
      log.error("Ошибка при удалении файла {} из MinIO: {}", fileName, e.getMessage());
    }
  }

  private void isBucketExists() throws Exception {
    boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    if (!found) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    }
  }
}
