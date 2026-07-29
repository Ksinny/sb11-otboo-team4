package com.sprint.mission.otboo.domain.social.feed.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedCreateRequest;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedDto;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedListParams;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedSortBy;
import com.sprint.mission.otboo.domain.social.feed.service.FeedService;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import com.sprint.mission.otboo.global.dto.SortDirection;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(FeedController.class)
@DisplayName("FeedController")
class FeedControllerTest {

  static final FixtureMonkey fm = FixtureMonkey.builder()
      .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
      .plugin(new JakartaValidationPlugin())
      .build();

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockitoBean
  FeedService feedService;

  @Nested
  @DisplayName("피드 등록 - POST /api/feeds")
  class CreateFeed {

    @Test
    @DisplayName("정상 요청이면 201과 FeedDto를 반환한다")
    void returns201AndFeedDto_whenRequestIsValid() throws Exception {
      // given
      UUID authorId = UUID.randomUUID();
      FeedCreateRequest request = fm.giveMeBuilder(FeedCreateRequest.class)
          .set("authorId", authorId)
          .sample();

      FeedDto response = new FeedDto(
          UUID.randomUUID(), Instant.now(), Instant.now(), "오늘의 착장", 0L, 0, false);
      when(feedService.create(any(FeedCreateRequest.class), eq(authorId))).thenReturn(response);

      // when & then
      mockMvc.perform(post("/api/feeds")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.content").value("오늘의 착장"))
          .andExpect(jsonPath("$.likeCount").value(0))
          .andExpect(jsonPath("$.likedByMe").value(false));
    }

    @Test
    @DisplayName("content가 비어 있으면 400을 반환한다")
    void returns400_whenContentIsBlank() throws Exception {
      // given — content 공백
      FeedCreateRequest request = new FeedCreateRequest(
          UUID.randomUUID(), UUID.randomUUID(), List.of(UUID.randomUUID()), "");

      // when & then
      mockMvc.perform(post("/api/feeds")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("피드 목록 조회 - GET /api/feeds")
  class GetFeedList {

    @Test
    @DisplayName("정상 요청이면 200과 CursorPageResponse를 반환한다")
    void returns200AndCursorPage_whenRequestIsValid() throws Exception {
      // given
      CursorPageResponse<FeedDto> response = new CursorPageResponse<>(
          List.of(), null, null, false, 0L, "createdAt", SortDirection.DESCENDING);
      when(feedService.getFeeds(any(FeedListParams.class))).thenReturn(response);

      // when & then
      mockMvc.perform(get("/api/feeds")
              .param("limit", "10")
              .param("sortBy", "createdAt")
              .param("sortDirection", "ASCENDING"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.hasNext").value(false))
          .andExpect(jsonPath("$.sortBy").value("createdAt"));

      ArgumentCaptor<FeedListParams> captor = ArgumentCaptor.forClass(FeedListParams.class);
      verify(feedService).getFeeds(captor.capture());
      FeedListParams captured = captor.getValue();
      assertThat(captured.limit()).isEqualTo(10);
      assertThat(captured.sortBy()).isEqualTo(FeedSortBy.CREATED_AT);
      assertThat(captured.sortDirection()).isEqualTo(SortDirection.ASCENDING);
    }

    @Test
    @DisplayName("limit이 1 미만이면 400을 반환한다")
    void returns400_whenLimitLessThanOne() throws Exception {
      // when & then
      mockMvc.perform(get("/api/feeds")
              .param("limit", "0")
              .param("sortBy", "createdAt")
              .param("sortDirection", "ASCENDING"))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 정렬 기준이면 400을 반환하고 서비스를 호출하지 않는다")
    void returns400AndDoesNotCallService_whenSortByUnknown() throws Exception {
      // when & then
      mockMvc.perform(get("/api/feeds")
              .param("limit", "10")
              .param("sortBy", "unknown")
              .param("sortDirection", "ASCENDING"))
          .andExpect(status().isBadRequest());

      verify(feedService, never()).getFeeds(any());
    }

    @Test
    @DisplayName("createdAt 정렬에 잘못된 형식의 커서면 400을 반환한다")
    void returns400_whenCreatedAtCursorInvalid() throws Exception {
      // when & then
      mockMvc.perform(get("/api/feeds")
              .param("limit", "10")
              .param("sortBy", "createdAt")
              .param("sortDirection", "DESCENDING")
              .param("cursor", "invalid-instant")
              .param("idAfter", UUID.randomUUID().toString()))
          .andExpect(status().isBadRequest());
    }
  }
}