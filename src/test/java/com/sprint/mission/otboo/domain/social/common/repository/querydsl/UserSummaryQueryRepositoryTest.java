package com.sprint.mission.otboo.domain.social.common.repository.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sprint.mission.otboo.domain.authuser.user.entity.Profile;
import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.authuser.user.exception.UserNotFoundException;
import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import com.sprint.mission.otboo.global.config.JpaConfig;
import com.sprint.mission.otboo.global.config.QuerydslConfig;
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
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QuerydslConfig.class, UserSummaryQueryRepository.class})
@DisplayName("UserSummaryQueryRepository")
class UserSummaryQueryRepositoryTest {

  @Autowired
  private UserSummaryQueryRepository userSummaryQueryRepository;

  @Autowired
  private TestEntityManager testEntityManager;

  @Nested
  @DisplayName("findByUserId")
  class FindByUserId {

    @Test
    @DisplayName("유저 ID로 name과 profileImageUrl을 채운 UserSummary를 반환한다")
    void 유저_ID로_name과_profileImageUrl을_채운_UserSummary를_반환한다() {
      // given
      User user = User.create("otboo", "otboo@test.com", "encoded-password");
      testEntityManager.persist(user);
      Profile profile = Profile.createDefault(user);
      testEntityManager.persist(profile);
      testEntityManager.flush();
      testEntityManager.clear();

      // when
      UserSummary result = userSummaryQueryRepository.findByUserId(user.getId());

      // then
      assertThat(result).isNotNull();
      assertThat(result.userId()).isEqualTo(user.getId());
      assertThat(result.name()).isEqualTo("otboo");
      assertThat(result.profileImageUrl()).isNull();
    }

    @Test
    @DisplayName("프로필 이미지가 있으면 profileImageUrl까지 채운 UserSummary를 반환한다")
    void 프로필_이미지가_있으면_profileImageUrl까지_채운_UserSummary를_반환한다() {
      // given
      User user = User.create("otboo", "otboo@test.com", "encoded-password");
      testEntityManager.persist(user);
      Profile profile = Profile.createDefault(user);
      ReflectionTestUtils.setField(profile, "profileImageUrl", "https://img.url/otboo.png");
      testEntityManager.persist(profile);
      testEntityManager.flush();
      testEntityManager.clear();

      // when
      UserSummary result = userSummaryQueryRepository.findByUserId(user.getId());

      // then
      assertThat(result.userId()).isEqualTo(user.getId());
      assertThat(result.name()).isEqualTo("otboo");
      assertThat(result.profileImageUrl()).isEqualTo("https://img.url/otboo.png");
    }

    @Test
    @DisplayName("존재하지 않는 userId면 UserNotFoundException을 던진다")
    void 존재하지_않는_userId면_UserNotFoundException을_던진다() {
      // given
      UUID unknownId = UUID.randomUUID();

      // when & then
      assertThatThrownBy(() -> userSummaryQueryRepository.findByUserId(unknownId))
          .isInstanceOf(UserNotFoundException.class);
    }
  }
}