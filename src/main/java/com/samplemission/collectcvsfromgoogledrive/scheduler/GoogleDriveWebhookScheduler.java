package com.samplemission.collectcvsfromgoogledrive.scheduler;

import com.samplemission.collectcvsfromgoogledrive.service.webhook.GoogleDriveWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Планировщик для управления Webhook Google Drive.
 *
 * <p>Этот класс отвечает за периодический перезапуск канала Webhook, используя {@link
 * GoogleDriveWebhookService}. Метод {@link #restartWebhookChannel()} будет вызываться автоматически
 * каждые 30 минут.
 */
@Component
@RequiredArgsConstructor
public class GoogleDriveWebhookScheduler {

  private final GoogleDriveWebhookService webhookService;

  /**
   * Перезапускает канал Webhook каждые 30 минут.
   *
   * <p>Этот метод вызывается с фиксированной задержкой в 1800000 миллисекунд (30 минут) для
   * обеспечения непрерывной работы канала Webhook.
   */
  @Scheduled(fixedDelay = 1800000)
  public void restartWebhookChannel() {
    webhookService.restartWebhookChannel();
  }
}
