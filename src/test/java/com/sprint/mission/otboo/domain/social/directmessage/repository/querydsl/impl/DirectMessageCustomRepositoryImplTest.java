package com.sprint.mission.otboo.domain.social.directmessage.repository.querydsl.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.social.directmessage.dto.DirectMessageParams;
import com.sprint.mission.otboo.domain.social.directmessage.entity.DirectMessage;
import com.sprint.mission.otboo.domain.social.directmessage.repository.DirectMessageRepository;
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
@DisplayName("DirectMessageCustomRepository")
class DirectMessageCustomRepositoryImplTest {

  @Autowired
  private DirectMessageRepository directMessageRepository;

  @Autowired
  private TestEntityManager testEntityManager;

  private User persistUser(String name) {
    return testEntityManager.persist(
        User.create(name, UUID.randomUUID() + "@otboo.io", "password"));
  }

  private DirectMessage saveMessage(UUID senderId, UUID receiverId, String content) {
    return directMessageRepository.save(DirectMessage.create(senderId, receiverId, content));
  }

  private void setCreatedAt(UUID messageId, Instant createdAt) {
    testEntityManager.getEntityManager()
        .createNativeQuery("update direct_messages set created_at = :createdAt where id = :id")
        .setParameter("createdAt", createdAt)
        .setParameter("id", messageId)
        .executeUpdate();
  }

  @Nested
  @DisplayName("findDirectMessages")
  class FindDirectMessages {

    @Test
    @DisplayName("두 사용자가 주고받은 메시지를 양방향으로 조회한다")
    void 두_사용자가_주고받은_메시지를_양방향으로_조회한다() {
      // given
      User me = persistUser("나");
      User other = persistUser("상대");
      User stranger = persistUser("제3자");

      DirectMessage sent = saveMessage(me.getId(), other.getId(), "내가 보낸 메시지");
      DirectMessage received = saveMessage(other.getId(), me.getId(), "상대가 보낸 메시지");
      saveMessage(me.getId(), stranger.getId(), "다른 사람과의 메시지");
      testEntityManager.flush();

      setCreatedAt(sent.getId(), Instant.parse("2026-08-07T07:00:00Z"));
      setCreatedAt(received.getId(), Instant.parse("2026-08-07T08:00:00Z"));
      testEntityManager.flush();
      testEntityManager.clear();

      DirectMessageParams params = new DirectMessageParams(other.getId(), null, null, 10);

      // when
      CursorPageResponse<DirectMessage> result =
          directMessageRepository.findDirectMessages(me.getId(), params);

      // then
      assertThat(result.data()).extracting(DirectMessage::getContent)
          .containsExactly("상대가 보낸 메시지", "내가 보낸 메시지");
      assertThat(result.totalCount()).isEqualTo(2L);
      assertThat(result.hasNext()).isFalse();
    }
  }
}