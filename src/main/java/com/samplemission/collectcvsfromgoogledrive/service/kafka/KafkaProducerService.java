package com.samplemission.collectcvsfromgoogledrive.service.kafka;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.samplemission.collectcvsfromgoogledrive.model.dto.ApplicantDto;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducerService {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Value("${spring.kafka.topic.name}")
  private String topic;

  public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.disable(
        com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  public void sendApplicantDtoToTopic(ApplicantDto applicantDto) {
    if (applicantDto != null) {
      try {
        String jsonMessage = objectMapper.writeValueAsString(applicantDto);
        kafkaTemplate.send(topic, jsonMessage);
        log.info("Сообщение отправлено в Kafka: {}", jsonMessage);
      } catch (JsonProcessingException e) {
        log.trace("Ошибка преобразования DTO в JSON: {}", e.getMessage());
      }
    }
  }

  public void sendApplicantDtoToTopic(List<ApplicantDto> applicantDtos) {
    if (applicantDtos != null && !applicantDtos.isEmpty()) {
      try {
        String jsonMessage = objectMapper.writeValueAsString(applicantDtos);
        kafkaTemplate.send(topic, jsonMessage);
        log.info("Сообщение отправлено в Kafka: {}", jsonMessage);
      } catch (JsonProcessingException e) {
        log.trace("Ошибка преобразования DTO в JSON: {}", e.getMessage());
      }
    }
  }
}
