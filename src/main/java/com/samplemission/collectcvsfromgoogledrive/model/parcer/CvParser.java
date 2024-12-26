package com.samplemission.collectcvsfromgoogledrive.model.parcer;

import com.samplemission.collectcvsfromgoogledrive.model.enums.DocumentTypeEnum;
import org.springframework.web.multipart.MultipartFile;

public interface CvParser<T> {
  DocumentTypeEnum type();

  T parseDocument(MultipartFile multipartFile);
}
