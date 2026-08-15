package com.sprint.mission.otboo.domain.social.feed.repository.elasticsearch.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.sprint.mission.otboo.domain.social.feed.document.FeedDocument;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedListParams;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedSearchResult;
import com.sprint.mission.otboo.domain.social.feed.repository.elasticsearch.FeedSearchCustomRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class FeedSearchCustomRepositoryImpl implements FeedSearchCustomRepository {

  private final ElasticsearchOperations operations;

  @Override
  public FeedSearchResult search(FeedListParams params) {
    Query query = StringUtils.hasText(params.keywordLike())
        ? Query.of(q -> q.match(m -> m.field("content").query(params.keywordLike())))
        : Query.of(q -> q.matchAll(m -> m));

    NativeQuery nativeQuery = NativeQuery.builder()
        .withQuery(query)
        .withMaxResults(params.limit() + 1)
        .withTrackTotalHits(true)
        .build();

    SearchHits<FeedDocument> hits = operations.search(nativeQuery, FeedDocument.class);

    List<UUID> feedIds = hits.getSearchHits().stream()
        .map(SearchHit::getId)
        .map(UUID::fromString)
        .toList();

    return new FeedSearchResult(feedIds, hits.getTotalHits(), null, null, false);
  }
}
