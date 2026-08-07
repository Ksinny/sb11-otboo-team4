package com.sprint.mission.otboo.domain.social.directmessage.repository.querydsl.impl;

import static com.sprint.mission.otboo.domain.social.directmessage.entity.QDirectMessage.directMessage;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.otboo.domain.social.directmessage.dto.DirectMessageParams;
import com.sprint.mission.otboo.domain.social.directmessage.entity.DirectMessage;
import com.sprint.mission.otboo.domain.social.directmessage.repository.querydsl.DirectMessageCustomRepository;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import com.sprint.mission.otboo.global.dto.SortDirection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DirectMessageCustomRepositoryImpl implements DirectMessageCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public CursorPageResponse<DirectMessage> findDirectMessages(UUID currentUserId,
      DirectMessageParams params) {
    List<DirectMessage> page = fetchDirectMessages(currentUserId, params);

    return new CursorPageResponse<>(page, null, null, false,
        countDirectMessages(currentUserId, params.userId()), "createdAt", SortDirection.DESCENDING
    );
  }

  private List<DirectMessage> fetchDirectMessages(UUID currentUserId,
      DirectMessageParams params) {
    return queryFactory
        .selectFrom(directMessage)
        .where(betweenUsers(currentUserId, params.userId()))
        .orderBy(directMessage.createdAt.desc(), directMessage.id.desc())
        .fetch();
  }

  private long countDirectMessages(UUID currentUserId, UUID otherUserId) {
    return Optional.ofNullable(
        queryFactory.select(directMessage.count())
            .from(directMessage)
            .where(betweenUsers(currentUserId, otherUserId))
            .fetchOne()
    ).orElse(0L);
  }

  // 발신/수신 방향 모두 포함
  private BooleanExpression betweenUsers(UUID one, UUID other) {
    return directMessage.senderId.eq(one).and(directMessage.receiverId.eq(other))
        .or(directMessage.senderId.eq(other).and(directMessage.receiverId.eq(one)));
  }
}