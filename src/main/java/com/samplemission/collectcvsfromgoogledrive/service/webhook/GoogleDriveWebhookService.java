package com.samplemission.collectcvsfromgoogledrive.service.webhook;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Channel;
import com.google.api.services.drive.model.StartPageToken;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Сервис для управления Webhook каналом Google Drive, позволяющий производить мониторинг изменения
 * в файлах Google Drive и обрабатывать их через канал Webhook.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleDriveWebhookService {

  private final Drive driveClient;
  private final GoogleDriveChangesProcessor googleDriveChangesProcessor;

  @Value("${google.drive.redirect.url}")
  private String webhookAddress;

  private static final String CHANNEL_TYPE = "web_hook";
  private static final String CHANGES_SPACES = "drive";
  private static final int CHANNEL_LIFETIME_MINUTES = 30;

  /**
   * Перезапускает канал Webhook, регистрируя его заново в Google Drive.
   *
   * <p>В случае успешного выполнения логируется токен страницы для мониторинга изменений. В случае
   * ошибки логируется сообщение с причиной ошибки.
   */
  public void restartWebhookChannel() {
    try {
      String pageToken = registerWebhook();
      log.info("Webhook channel restarted successfully. Page token: {}", pageToken);
    } catch (IOException e) {
      log.error("Failed to restart webhook channel: {}", e.getMessage());
    }
  }

  /**
   * Регистрирует канал Webhook на Google Drive и обновляет информацию о канале.
   *
   * @return Токен страницы для отслеживания изменений в Google Drive.
   * @throws IOException если произошла ошибка ввода-вывода при регистрации Webhook.
   */
  private String registerWebhook() throws IOException {
    StartPageToken response = driveClient.changes().getStartPageToken().execute();
    String pageToken = response.getStartPageToken();

    Channel executed =
        driveClient.changes().watch(pageToken, createChannel()).setSpaces(CHANGES_SPACES).execute();

    googleDriveChangesProcessor.updateChannelData(
        pageToken, executed.getId(), executed.getResourceId());

    return pageToken;
  }

  /**
   * Создает и настраивает новый объект канала Webhook для Google Drive.
   *
   * <p>Включает уникальный идентификатор канала, тип канала, время его завершения и адрес Webhook,
   * куда будут отправляться уведомления об изменениях.
   *
   * @return Новый объект канала Webhook с настроенными параметрами.
   */
  private Channel createChannel() {
    long expirationTimeInMillis =
        LocalDateTime.now()
            .plusMinutes(CHANNEL_LIFETIME_MINUTES)
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli();

    Channel channel = new Channel();
    channel.setId(UUID.randomUUID().toString());
    channel.setType(CHANNEL_TYPE);
    channel.setExpiration(expirationTimeInMillis);
    channel.setAddress(webhookAddress);

    return channel;
  }
}
