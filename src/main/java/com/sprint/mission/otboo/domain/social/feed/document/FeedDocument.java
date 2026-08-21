package com.sprint.mission.otboo.domain.social.feed.document;

import com.sprint.mission.otboo.domain.social.feed.dto.OotdSnapshot;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.weathernotification.weather.entity.enums.PrecipitationType;
import com.sprint.mission.otboo.domain.weathernotification.weather.entity.enums.SkyStatus;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.data.annotation.Id;
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

  @Field(type = FieldType.Text, analyzer = "korean")
  private String searchText;

  @Field(type = FieldType.Text, analyzer = "korean", copyTo = "searchText")
  private String content;

  @Field(type = FieldType.Text, analyzer = "korean", copyTo = "searchText")
  private String ootdNames;

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
    doc.ootdNames = joinOotdNames(feed.getOotds());
    doc.authorId = feed.getAuthorId().toString();
    doc.skyStatus = feed.getSkyStatus();
    doc.precipitationType = feed.getPrecipitationType();
    doc.createdAt = feed.getCreatedAt();
    doc.likeCount = feed.getLikeCount();
    return doc;
  }

  // 본문에 없는 착장 정보도 검색되도록 이름만 이어 붙인다.
  // copy_to로 content와 함께 searchText에 모이므로 검색은 한 필드만 본다.
  private static String joinOotdNames(List<OotdSnapshot> ootds) {
    if (ootds == null || ootds.isEmpty()) {
      return null;
    }
    return ootds.stream()
        .map(OotdSnapshot::name)
        .filter(Objects::nonNull)
        .collect(Collectors.joining(" "));
  }
}
