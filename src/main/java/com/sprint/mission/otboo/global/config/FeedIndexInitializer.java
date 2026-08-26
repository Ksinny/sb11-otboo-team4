package com.sprint.mission.otboo.global.config;

import com.sprint.mission.otboo.domain.social.feed.document.FeedDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActionParameters;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;

/**
 * 피드 검색 인덱스를 명시적으로 생성한다.
 *
 * <p>Spring Data Elasticsearch의 자동 생성(@Document(createIndex = true))에 의존하면
 * 인덱스 생성 주체가 코드에 드러나지 않고, 다중 인스턴스가 동시에 기동할 때 경합이 발생한다.
 *
 * <p>인덱스는 {@code feeds_v{n}}으로 만들고 {@code feeds}를 alias로 붙인다. 매핑을 바꿀 때
 * 새 인덱스를 만들어 alias만 옮기면 검색이 끊기지 않는다. 애플리케이션은 alias만 보므로 {@code @Document(indexName)}은 그대로
 * {@code feeds}다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeedIndexInitializer implements ApplicationRunner {

  private static final String ALREADY_EXISTS = "resource_already_exists_exception";
  private static final String INITIAL_INDEX_NAME = FeedDocument.INDEX_NAME + "_v1";
  private static final String MIGRATION_REQUIRED_MARKER = "FEED_INDEX_MIGRATION_REQUIRED";

  private final ElasticsearchOperations elasticsearchOperations;

  @Override
  public void run(ApplicationArguments args) {
    IndexOperations aliasOps =
        elasticsearchOperations.indexOps(IndexCoordinates.of(FeedDocument.INDEX_NAME));

    if (aliasOps.exists()) {
      warnIfNotAlias(aliasOps);
      return;
    }

    createInitialIndex();
  }

  // exists()는 HEAD 요청이라 alias와 실제 인덱스를 구분하지 않는다.
  // alias 구조 도입 전에 만들어진 인덱스가 남아 있으면 매핑 마이그레이션 배치를 쓸 수 없으므로 로그로 드러낸다.
  private void warnIfNotAlias(IndexOperations aliasOps) {
    if (isAlias(aliasOps)) {
      return;
    }
    log.warn("{}: {}가 alias가 아닌 실제 인덱스입니다. 매핑 마이그레이션 전에 전환이 필요합니다.",
        MIGRATION_REQUIRED_MARKER, FeedDocument.INDEX_NAME);
  }

  // getAliases는 alias가 아니면 ResourceNotFoundException을 던진다.
  // "alias가 아니다"는 정상 상태이므로 예외를 흡수해 boolean으로 바꾼다.
  private boolean isAlias(IndexOperations aliasOps) {
    try {
      return !aliasOps.getAliases(FeedDocument.INDEX_NAME).isEmpty();
    } catch (ResourceNotFoundException e) {
      return false;
    }
  }

  private void createInitialIndex() {
    IndexOperations entityOps = elasticsearchOperations.indexOps(FeedDocument.class);
    IndexOperations targetOps =
        elasticsearchOperations.indexOps(IndexCoordinates.of(INITIAL_INDEX_NAME));

    try {
      targetOps.create(entityOps.createSettings(), entityOps.createMapping());
      targetOps.alias(addAliasAction());
      log.info("피드 검색 인덱스 생성 완료: index={}, alias={}",
          INITIAL_INDEX_NAME, FeedDocument.INDEX_NAME);
    } catch (DataAccessException e) {
      // 다중 인스턴스 동시 기동 시 다른 인스턴스가 먼저 생성한 경우만 흡수한다.
      // 연결 실패나 매핑 오류는 그대로 전파해 기동 실패로 드러나게 한다.
      if (!isAlreadyExists(e)) {
        throw e;
      }
      log.warn("피드 검색 인덱스가 이미 존재합니다: index={}", INITIAL_INDEX_NAME);
    }
  }

  private AliasActions addAliasAction() {
    return new AliasActions(new AliasAction.Add(
        AliasActionParameters.builder()
            .withIndices(INITIAL_INDEX_NAME)
            .withAliases(FeedDocument.INDEX_NAME)
            .build()));
  }

  private boolean isAlreadyExists(DataAccessException e) {
    String message = e.getMessage();
    return message != null && message.contains(ALREADY_EXISTS);
  }
}
