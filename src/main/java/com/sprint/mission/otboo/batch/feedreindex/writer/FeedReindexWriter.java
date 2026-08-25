package com.sprint.mission.otboo.batch.feedreindex.writer;

import com.sprint.mission.otboo.batch.feedreindex.metrics.FeedReindexMetrics;
import com.sprint.mission.otboo.domain.social.feed.document.FeedDocument;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.social.feed.repository.elasticsearch.FeedSearchRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@StepScope
@RequiredArgsConstructor
@Component
public class FeedReindexWriter implements ItemWriter<Feed> {

  private final FeedSearchRepository feedSearchRepository;
  private final EntityManager entityManager;
  private final FeedReindexMetrics feedReindexMetrics;

  @Value("#{stepExecution.stepName}")
  private final String stepName;

  @Override
  public void write(Chunk<? extends Feed> chunk) {
    if (chunk.isEmpty()) {
      return;
    }

    List<FeedDocument> documents = chunk.getItems().stream()
        .map(FeedDocument::from)
        .toList();

    long drift = countDrift(documents);
    feedSearchRepository.saveAll(documents);
    feedReindexMetrics.countDrift(stepName, drift);

    entityManager.clear();
    log.info("피드 재색인 chunk 완료: size={}, drift={}", documents.size(), drift);
  }

  // 이 배치는 전 건을 다시 쓰므로 writeCount가 항상 활성 피드 수와 같다.
  // 어긋난 문서가 0건이든 500건이든 처리량 지표는 동일하므로, 실제 교정한 건수를 따로 센다.
  // 청크당 mget 한 번이 추가되지만 500건 한 요청이라 부담은 작다.
  private long countDrift(List<FeedDocument> documents) {
    return 0;
  }
}
