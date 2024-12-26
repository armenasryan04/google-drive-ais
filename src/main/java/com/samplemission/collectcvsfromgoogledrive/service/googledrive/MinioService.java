package com.samplemission.collectcvsfromgoogledrive.service.googledrive;

import java.io.InputStream;
import java.util.Set;

public interface MinioService {
  void deleteFile(String filename);

  Set<String> getMinioFiles() throws Exception;

  void uploadFile(String fileName, InputStream inputStream) throws Exception;
}
