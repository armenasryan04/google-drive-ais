package com.samplemission.collectcvsfromgoogledrive.scheduler;

import com.samplemission.collectcvsfromgoogledrive.service.googledrive.FileSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileSyncScheduler {

  private final FileSyncService fileSyncService;

  @Scheduled(cron = "#{@scheduleConfig.cron}", zone = "Europe/Moscow")
  public void syncFiles() {
    try {
      fileSyncService.syncFiles();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }
}
