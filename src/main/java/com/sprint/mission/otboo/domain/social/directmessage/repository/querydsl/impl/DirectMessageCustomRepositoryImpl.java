package com.sprint.mission.otboo.domain.social.directmessage.repository.querydsl.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.otboo.domain.social.directmessage.dto.DirectMessageParams;
import com.sprint.mission.otboo.domain.social.directmessage.entity.DirectMessage;
import com.sprint.mission.otboo.domain.social.directmessage.repository.querydsl.DirectMessageCustomRepository;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import com.sprint.mission.otboo.global.dto.SortDirection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DirectMessageCustomRepositoryImpl implements DirectMessageCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public CursorPageResponse<DirectMessage> findDirectMessages(UUID currentUserId,
      DirectMessageParams params) {
    return new CursorPageResponse<>(List.of(), null, null, false, 0L,
        "createdAt", SortDirection.DESCENDING);
  }
}