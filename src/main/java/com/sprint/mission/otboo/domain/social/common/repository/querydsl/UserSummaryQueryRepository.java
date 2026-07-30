package com.sprint.mission.otboo.domain.social.common.repository.querydsl;

import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserSummaryQueryRepository {

  public UserSummary findByUserId(UUID userId) {
    return null;
  }
}