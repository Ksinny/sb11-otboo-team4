package com.sprint.mission.otboo.domain.social.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import com.sprint.mission.otboo.domain.social.common.repository.querydsl.UserSummaryQueryRepository;
import com.sprint.mission.otboo.domain.social.feed.dto.CommentCreateRequest;
import com.sprint.mission.otboo.domain.social.feed.dto.CommentDto;
import com.sprint.mission.otboo.domain.social.feed.entity.Comment;
import com.sprint.mission.otboo.domain.social.feed.mapper.CommentMapper;
import com.sprint.mission.otboo.domain.social.feed.repository.CommentRepository;
import com.sprint.mission.otboo.domain.social.feed.repository.FeedRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService")
class CommentServiceTest {

  static final FixtureMonkey fm = FixtureMonkey.builder()
      .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
      .plugin(new JakartaValidationPlugin())
      .build();

  @Mock
  CommentRepository commentRepository;
  @Mock
  FeedRepository feedRepository;
  @Mock
  CommentMapper commentMapper;
  @Mock
  UserSummaryQueryRepository userSummaryQueryRepository;

  @InjectMocks
  CommentService commentService;

  @Nested
  @DisplayName("댓글 등록")
  class Create {

    @Test
    @DisplayName("댓글을 저장하고 CommentDto를 반환한다")
    void 댓글을_저장하고_CommentDto를_반환한다() {
      // given
      UUID feedId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      CommentCreateRequest request = fm.giveMeBuilder(CommentCreateRequest.class)
          .set("feedId", feedId)
          .set("authorId", userId)
          .set("content", "댓글 내용")
          .sample();

      Comment saved = Comment.create(feedId, userId, "댓글 내용");
      given(commentRepository.save(any(Comment.class))).willReturn(saved);

      CommentDto expected = new CommentDto(UUID.randomUUID(), null, feedId, null, "댓글 내용");
      given(commentMapper.toDto(saved, null)).willReturn(expected);

      // when
      CommentDto result = commentService.create(request, userId);

      // then
      assertThat(result).isEqualTo(expected);
      verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글을 저장하면 피드의 댓글 카운트를 증가시킨다")
    void 댓글을_저장하면_피드의_댓글_카운트를_증가시킨다() {
      // given
      UUID feedId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      CommentCreateRequest request = fm.giveMeBuilder(CommentCreateRequest.class)
          .set("feedId", feedId)
          .set("authorId", userId)
          .set("content", "댓글 내용")
          .sample();

      Comment saved = Comment.create(feedId, userId, "댓글 내용");
      given(commentRepository.save(any(Comment.class))).willReturn(saved);

      // when
      commentService.create(request, userId);

      // then
      verify(feedRepository).incrementCommentCount(feedId);
    }

    @Test
    @DisplayName("반환하는 CommentDto에 작성자 정보를 채운다")
    void 반환하는_CommentDto에_작성자_정보를_채운다() {
      // given
      UUID feedId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      CommentCreateRequest request = fm.giveMeBuilder(CommentCreateRequest.class)
          .set("feedId", feedId)
          .set("authorId", userId)
          .set("content", "댓글 내용")
          .sample();

      Comment saved = Comment.create(feedId, userId, "댓글 내용");
      given(commentRepository.save(any(Comment.class))).willReturn(saved);

      UserSummary author = new UserSummary(userId, "경신", null);
      given(userSummaryQueryRepository.findByUserId(userId)).willReturn(author);

      CommentDto expected = new CommentDto(UUID.randomUUID(), null, feedId, author, "댓글 내용");
      given(commentMapper.toDto(saved, author)).willReturn(expected);

      // when
      CommentDto result = commentService.create(request, userId);

      // then
      assertThat(result).isEqualTo(expected);
    }
  }
}