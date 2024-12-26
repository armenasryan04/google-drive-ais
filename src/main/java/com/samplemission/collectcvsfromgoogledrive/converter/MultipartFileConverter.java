package com.samplemission.collectcvsfromgoogledrive.converter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * Конвертер для преобразования {@link InputStream} в {@link MultipartFile}.
 *
 * <p>Этот класс предоставляет метод для преобразования входного потока в объект, реализующий
 * интерфейс MultipartFile.
 */
public class MultipartFileConverter {

  /**
   * Преобразует {@link InputStream} в {@link CustomMultipartFile}.
   *
   * @param inputStream Входной поток, содержащий данные файла.
   * @param originalFilename Исходное имя файла.
   * @param contentType Тип содержимого файла.
   * @return Объект {@link CustomMultipartFile}, содержащий данные файла.
   */
  @SneakyThrows
  public static CustomMultipartFile convert(
      InputStream inputStream, String originalFilename, String contentType) {
    byte[] content = inputStream.readAllBytes();
    return new CustomMultipartFile(originalFilename, contentType, content);
  }

  /**
   * Реализация интерфейса {@link MultipartFile}.
   *
   * <p>Этот класс представляет файл в виде массива байтов.
   */
  public static class CustomMultipartFile implements MultipartFile {

    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    /**
     * Конструктор для создания экземпляра {@link CustomMultipartFile}.
     *
     * @param originalFilename Исходное имя файла.
     * @param contentType Тип содержимого файла.
     * @param content Данные файла в виде массива байтов.
     */
    public CustomMultipartFile(String originalFilename, String contentType, byte[] content) {
      this.originalFilename = originalFilename;
      this.contentType = contentType;
      this.content = content;
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public String getOriginalFilename() {
      return originalFilename;
    }

    @Override
    public String getContentType() {
      return contentType;
    }

    @Override
    public boolean isEmpty() {
      return content.length == 0;
    }

    @Override
    public long getSize() {
      return content.length;
    }

    @Override
    public byte[] getBytes() {
      return content;
    }

    @Override
    public InputStream getInputStream() {
      return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(@NotNull java.io.File dest) throws IllegalStateException {}
  }
}
