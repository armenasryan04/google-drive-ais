package com.samplemission.collectcvsfromgoogledrive.config;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DateConfig {
  @Bean
  public DateTimeFormatter localDatePatternRu() {
    return DateTimeFormatter.ofPattern("d MMMM yyyy").withLocale(new Locale("RU"));
  }

  @Bean
  public DateTimeFormatter monthYearPatternRu() {
    return DateTimeFormatter.ofPattern("LLLL uuuu").withLocale(new Locale("RU"));
  }
}
