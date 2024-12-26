package com.samplemission.collectcvsfromgoogledrive.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурационный класс для настройки интеграции с Google Drive API.
 *
 * <p>Этот класс инициализирует необходимые компоненты для взаимодействия с Google Drive, включая
 * получение токена доступа и создание экземпляра {@link Drive} для выполнения запросов.
 */
@Configuration
public class GoogleDriveConfig {

  @Value("${google.cloud.credentials.path}")
  private String filePath;

  /**
   * Получение токена доступа для взаимодействия с Google Drive API.
   *
   * <p>Этот метод создает {@link GoogleCredentials} на основе указанных учетных данных и проверяет
   * его на истечение срока действия. Если токен истек, он будет обновлен.
   *
   * @return Токен доступа, используемый для авторизации запросов к Google Drive API.
   * @throws IOException Если возникла ошибка при чтении учетных данных или получении токена.
   */
  private String getAccessToken() throws IOException {
    GoogleCredentials credentials =
        GoogleCredentials.fromStream(new FileInputStream(filePath))
            .createScoped(
                Arrays.asList(
                    "https://www.googleapis.com/auth/drive",
                    "https://www.googleapis.com/auth/drive.metadata.readonly"));
    credentials.refreshIfExpired();
    return credentials.getAccessToken().getTokenValue();
  }

  /**
   * Инициализирует {@link HttpRequestInitializer} для авторизации запросов с использованием токена.
   *
   * <p>Этот метод создает объект, который будет добавлять заголовок авторизации с токеном доступа
   * для каждого HTTP-запроса, отправляемого в Google Drive API.
   *
   * @return {@link HttpRequestInitializer}, который добавляет заголовок авторизации для запросов.
   * @throws IOException Если возникла ошибка при получении токена доступа.
   */
  private HttpRequestInitializer getRequestInitializer() throws IOException {
    String accessToken = getAccessToken();
    return request -> {
      request.getHeaders().setAuthorization("Bearer " + accessToken);
    };
  }

  /**
   * Создает и возвращает экземпляр {@link Drive} для взаимодействия с Google Drive API.
   *
   * <p>Этот метод настраивает необходимые компоненты, такие как транспорт, фабрику JSON и
   * инициализирует запросы с токеном доступа для выполнения операций с файлами Google Drive.
   *
   * @return Экземпляр {@link Drive} для работы с Google Drive API.
   * @throws IOException Если возникла ошибка при инициализации транспорта или получения токена.
   * @throws GeneralSecurityException Если возникла ошибка при создании безопасного транспортного
   *     компонента.
   */
  @Bean
  public Drive googleDrive() throws IOException, GeneralSecurityException {
    // Настройка транспорта и фабрики JSON
    HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    // Создание экземпляра Google Drive API
    return new Drive.Builder(transport, jsonFactory, getRequestInitializer())
        .setApplicationName("GoogleDriveReader")
        .build();
  }
}
