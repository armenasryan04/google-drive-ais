package com.samplemission.collectcvsfromgoogledrive.listener;

import com.samplemission.collectcvsfromgoogledrive.service.googledrive.FileSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationStartListener {

  private final FileSyncService fileSyncService;

  @EventListener(ApplicationReadyEvent.class)
  public void applicationStart() throws Exception {
    fileSyncService.syncFiles();
  }
}
