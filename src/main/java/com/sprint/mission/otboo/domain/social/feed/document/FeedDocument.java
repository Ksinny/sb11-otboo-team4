package com.sprint.mission.otboo.domain.social.feed.document;

import com.sprint.mission.otboo.domain.social.feed.dto.OotdSnapshot;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.weathernotification.weather.entity.enums.PrecipitationType;
import com.sprint.mission.otboo.domain.weathernotification.weather.entity.enums.SkyStatus;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Getter
@Setting(settingPath = "elasticsearch/feed-settings.json")
@Document(indexName = "feeds", createIndex = false)
public class FeedDocument {

  @Id
  @Field(type = FieldType.Keyword)
  private String id;

  /**
   * 키워드 검색 대상. {@code copy_to}로만 채워지므로 애플리케이션이 직접 값을 넣지 않는다.
   *
   * <p>사용자가 검색어로 입력할 법한 자유 텍스트만 모은다. enum·id처럼 필터로 다루는 값은
   * 검색어가 될 일이 없으면서 토큰만 늘려 {@code minimum_should_match}를 가혹하게 만든다.
   */
  @ReadOnlyProperty
  @Field(type = FieldType.Text, analyzer = "korean")
  private String searchText;

  @Field(type = FieldType.Text, analyzer = "korean", copyTo = "searchText")
  private String content;

  @Field(type = FieldType.Text, analyzer = "korean", copyTo = "searchText")
  private List<String> ootdNames;

  @Field(type = FieldType.Keyword)
  private String authorId;

  @Field(type = FieldType.Keyword)
  private SkyStatus skyStatus;

  @Field(type = FieldType.Keyword)
  private PrecipitationType precipitationType;

  @Field(type = FieldType.Date)
  private Instant createdAt;

  @Field(type = FieldType.Long)
  private long likeCount;

  public static FeedDocument from(Feed feed) {
    FeedDocument doc = new FeedDocument();
    doc.id = feed.getId().toString();
    doc.content = feed.getContent();
    doc.ootdNames = extractOotdNames(feed.getOotds());
    doc.authorId = feed.getAuthorId().toString();
    doc.skyStatus = feed.getSkyStatus();
    doc.precipitationType = feed.getPrecipitationType();
    doc.createdAt = feed.getCreatedAt();
    doc.likeCount = feed.getLikeCount();
    return doc;
  }

  // 본문에 없는 착장 정보도 검색되도록 이름을 함께 색인한다.
  private static List<String> extractOotdNames(List<OotdSnapshot> ootds) {
    if (ootds == null) {
      return List.of();
    }
    return ootds.stream()
        .map(OotdSnapshot::name)
        .filter(Objects::nonNull)
        .toList();
  }

  /**
   * ES에 저장된 문서가 이 문서(DB 기준)와 정합한지 판정한다.
   *
   * <p>생성 후 바뀌지 않는 필드({@code id}·{@code authorId}·{@code createdAt}·날씨 스냅샷)는
   * 비교하지 않는다. 문서가 존재하면 그 값은 맞고, {@code createdAt}은 DB가 마이크로초, ES가 밀리초라 왕복 시 값이 달라져 없는 불일치를 만든다.
   *
   * <p>이벤트 기반 인덱싱이 놓칠 수 있는 것은 수정({@code content}·{@code ootdNames})과
   * 좋아요({@code likeCount})이므로 그 셋만 본다.
   */
  public boolean isConsistentWith(FeedDocument indexed) {
    return indexed != null
        && Objects.equals(content, indexed.content)
        && Objects.equals(ootdNames, indexed.ootdNames)
        && likeCount == indexed.likeCount;
  }
}
