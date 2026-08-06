package com.sprint.mission.otboo.domain.social.feed.repository.querydsl.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.domain.social.feed.dto.FeedCommentParams;
import com.sprint.mission.otboo.domain.social.feed.entity.Comment;
import com.sprint.mission.otboo.domain.social.feed.repository.CommentRepository;
import com.sprint.mission.otboo.global.config.JpaConfig;
import com.sprint.mission.otboo.global.config.QuerydslConfig;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QuerydslConfig.class})
@DisplayName("CommentCustomRepository")
class CommentCustomRepositoryImplTest {

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private TestEntityManager testEntityManager;

  private Comment saveComment(UUID feedId, String content) {
    return commentRepository.save(Comment.create(feedId, UUID.randomUUID(), content));
  }

  private void setCreatedAt(UUID commentId, Instant createdAt) {
    testEntityManager.getEntityManager()
        .createNativeQuery("update comments set created_at = :createdAt where id = :id")
        .setParameter("createdAt", createdAt)
        .setParameter("id", commentId)
        .executeUpdate();
  }

  @Nested
  @DisplayName("findComments")
  class FindComments {

    @Test
    @DisplayName("해당 피드의 댓글을 createdAt 내림차순으로 반환한다")
    void 해당_피드의_댓글을_createdAt_내림차순으로_반환한다() {
      // given
      UUID feedId = UUID.randomUUID();
      Comment older = saveComment(feedId, "오래된 댓글");
      Comment newer = saveComment(feedId, "최신 댓글");
      setCreatedAt(older.getId(), Instant.parse("2026-08-05T07:00:00Z"));
      setCreatedAt(newer.getId(), Instant.parse("2026-08-05T08:00:00Z"));
      saveComment(UUID.randomUUID(), "다른 피드 댓글");

      FeedCommentParams params = new FeedCommentParams(feedId, null, null, 10);

      // when
      CursorPageResponse<Comment> result = commentRepository.findComments(params);

      // then
      assertThat(result.data()).extracting(Comment::getContent)
          .containsExactly("최신 댓글", "오래된 댓글");
      assertThat(result.totalCount()).isEqualTo(2L);
      assertThat(result.hasNext()).isFalse();
    }

    @Test
    @DisplayName("limit보다 많으면 hasNext가 true이고 다음 커서로 이어서 조회한다")
    void limit보다_많으면_hasNext가_true이고_다음_커서로_이어서_조회한다() {
      // given — 최신순: c3(08:00) > c2(07:00) > c1(06:00)
      UUID feedId = UUID.randomUUID();
      Comment c1 = saveComment(feedId, "댓글1");
      Comment c2 = saveComment(feedId, "댓글2");
      Comment c3 = saveComment(feedId, "댓글3");
      testEntityManager.flush();

      setCreatedAt(c1.getId(), Instant.parse("2026-08-05T06:00:00Z"));
      setCreatedAt(c2.getId(), Instant.parse("2026-08-05T07:00:00Z"));
      setCreatedAt(c3.getId(), Instant.parse("2026-08-05T08:00:00Z"));
      testEntityManager.flush();
      testEntityManager.clear();

      // when — 첫 페이지 (limit 2)
      FeedCommentParams firstPage = new FeedCommentParams(feedId, null, null, 2);
      CursorPageResponse<Comment> first = commentRepository.findComments(firstPage);

      // then — 최신 2개 + hasNext
      assertThat(first.data()).extracting(Comment::getContent)
          .containsExactly("댓글3", "댓글2");
      assertThat(first.hasNext()).isTrue();
      assertThat(first.nextCursor()).isNotNull();
      assertThat(first.totalCount()).isEqualTo(3L);

      // when — 다음 페이지
      FeedCommentParams nextPage = new FeedCommentParams(
          feedId, first.nextCursor(), first.nextIdAfter(), 2);
      CursorPageResponse<Comment> second = commentRepository.findComments(nextPage);

      // then — 남은 1개
      assertThat(second.data()).extracting(Comment::getContent)
          .containsExactly("댓글1");
      assertThat(second.hasNext()).isFalse();
    }
  }
}