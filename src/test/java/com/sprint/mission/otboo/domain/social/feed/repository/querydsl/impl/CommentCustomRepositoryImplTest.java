package com.sprint.mission.otboo.domain.social.feed.repository.querydsl;

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
  }
}