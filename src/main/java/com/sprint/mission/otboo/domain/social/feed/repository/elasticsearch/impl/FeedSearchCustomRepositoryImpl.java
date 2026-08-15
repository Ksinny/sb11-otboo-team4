package com.sprint.mission.otboo.domain.social.feed.repository.elasticsearch.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.sprint.mission.otboo.domain.social.feed.document.FeedDocument;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedListParams;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedSearchResult;
import com.sprint.mission.otboo.domain.social.feed.repository.elasticsearch.FeedSearchCustomRepository;
import java.util.ArrayList;
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

  private static final String FIELD_CONTENT = "content";
  private static final String FIELD_SKY_STATUS = "skyStatus";
  private static final String FIELD_PRECIPITATION_TYPE = "precipitationType";
  private static final String FIELD_AUTHOR_ID = "authorId";

  private final ElasticsearchOperations operations;

  @Override
  public FeedSearchResult search(FeedListParams params) {
    SearchHits<FeedDocument> hits =
        operations.search(buildNativeQuery(params), FeedDocument.class);
    return toSearchResult(hits);
  }

  private NativeQuery buildNativeQuery(FeedListParams params) {
    return NativeQuery.builder()
        .withQuery(buildQuery(params))
        .withMaxResults(params.limit() + 1)
        .withTrackTotalHits(true)
        .build();
  }

  private Query buildQuery(FeedListParams params) {
    return Query.of(q -> q.bool(b -> b
        .must(buildKeywordQuery(params.keywordLike()))
        .filter(buildFilters(params))));
  }

  private Query buildKeywordQuery(String keywordLike) {
    if (!StringUtils.hasText(keywordLike)) {
      return Query.of(q -> q.matchAll(m -> m));
    }
    return Query.of(q -> q.match(m -> m.field(FIELD_CONTENT).query(keywordLike)));
  }

  private List<Query> buildFilters(FeedListParams params) {
    List<Query> filters = new ArrayList<>();
    addTermFilter(filters, FIELD_SKY_STATUS, params.skyStatusEqual());
    addTermFilter(filters, FIELD_PRECIPITATION_TYPE, params.precipitationTypeEqual());
    addTermFilter(filters, FIELD_AUTHOR_ID, params.authorIdEqual());
    return filters;
  }

  // enum은 name()으로 인덱싱되므로 상수명으로 비교한다.
  private void addTermFilter(List<Query> filters, String field, Enum<?> value) {
    if (value != null) {
      filters.add(termFilter(field, value.name()));
    }
  }

  // UUID는 문자열로 인덱싱되므로 toString()으로 비교한다.
  private void addTermFilter(List<Query> filters, String field, UUID value) {
    if (value != null) {
      filters.add(termFilter(field, value.toString()));
    }
  }

  private Query termFilter(String field, String value) {
    return Query.of(q -> q.term(t -> t.field(field).value(value)));
  }

  private FeedSearchResult toSearchResult(SearchHits<FeedDocument> hits) {
    List<UUID> feedIds = hits.getSearchHits().stream()
        .map(SearchHit::getId)
        .map(UUID::fromString)
        .toList();
    return new FeedSearchResult(feedIds, hits.getTotalHits(), null, null, false);
  }
}
