package com.sprint.mission.otboo.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedIndexInitializer implements ApplicationRunner {

  private final ElasticsearchOperations elasticsearchOperations;

  @Override
  public void run(ApplicationArguments args) {
  }
}
