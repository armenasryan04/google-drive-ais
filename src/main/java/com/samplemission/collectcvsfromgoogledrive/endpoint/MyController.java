package com.samplemission.collectcvsfromgoogledrive.endpoint;

import com.samplemission.collectcvsfromgoogledrive.model.dto.ApplicantDto;
import com.samplemission.collectcvsfromgoogledrive.model.parcer.ResumeWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MyController {
  private final ResumeWriter resumeWriter;

  @PostMapping("/submit")
  public ResponseEntity<ApplicantDto> submitData(@RequestBody ApplicantDto applicantDto)
      throws Exception {

    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    resumeWriter.writeToDocx(applicantDto, "C:\\Users\\Lenovo\\Desktop\\" + timestamp + ".docx");
    return ResponseEntity.ok(applicantDto);
  }
}
