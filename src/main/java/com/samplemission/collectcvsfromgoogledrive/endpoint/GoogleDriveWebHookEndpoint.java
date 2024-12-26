package com.samplemission.collectcvsfromgoogledrive.endpoint;

import com.samplemission.collectcvsfromgoogledrive.service.webhook.GoogleDriveChangesProcessor;
import javax.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GoogleDriveWebHookEndpoint {

  private final GoogleDriveChangesProcessor googleDriveChangesProcessor;

  public GoogleDriveWebHookEndpoint(GoogleDriveChangesProcessor googleDriveChangesProcessor) {
    this.googleDriveChangesProcessor = googleDriveChangesProcessor;
  }

  @PostMapping("/webhook")
  public ResponseEntity<?> handleDriveNotification(
      @RequestHeader("X-Goog-Channel-ID") @NotBlank String channelId,
      @RequestHeader("X-Goog-Resource-ID") @NotBlank String resourceId) {
    googleDriveChangesProcessor.processChanges(channelId, resourceId);
    return ResponseEntity.accepted().body("Received notification");
  }
}
