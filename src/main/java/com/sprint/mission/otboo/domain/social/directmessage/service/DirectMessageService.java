package com.sprint.mission.otboo.domain.social.directmessage.service;

import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import com.sprint.mission.otboo.domain.social.common.repository.querydsl.UserSummaryQueryRepository;
import com.sprint.mission.otboo.domain.social.directmessage.dto.DirectMessageDto;
import com.sprint.mission.otboo.domain.social.directmessage.dto.DirectMessageParams;
import com.sprint.mission.otboo.domain.social.directmessage.dto.DirectMessageSendRequest;
import com.sprint.mission.otboo.domain.social.directmessage.entity.DirectMessage;
import com.sprint.mission.otboo.domain.social.directmessage.exception.DirectMessageForbiddenException;
import com.sprint.mission.otboo.domain.social.directmessage.exception.SelfDirectMessageNotAllowedException;
import com.sprint.mission.otboo.domain.social.directmessage.mapper.DirectMessageMapper;
import com.sprint.mission.otboo.domain.social.directmessage.repository.DirectMessageRepository;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class DirectMessageService {

  private final DirectMessageRepository directMessageRepository;
  private final DirectMessageMapper directMessageMapper;
  private final UserSummaryQueryRepository userSummaryQueryRepository;

  public CursorPageResponse<DirectMessageDto> getDirectMessages(UUID currentUserId,
      DirectMessageParams params) {
    return toDtoPage(directMessageRepository.findDirectMessages(currentUserId, params));
  }

  @Transactional
  public DirectMessageDto send(DirectMessageSendRequest request, UUID currentUserId) {
    if (!request.senderId().equals(currentUserId)) {
      throw DirectMessageForbiddenException.senderMismatch(currentUserId, request.senderId());
    }
    if (request.senderId().equals(request.receiverId())) {
      throw SelfDirectMessageNotAllowedException.withNone();
    }

    DirectMessage message = directMessageRepository.save(
        DirectMessage.create(request.senderId(), request.receiverId(), request.content()));
    log.info("DM 전송 완료: dmId={}", message.getId());

    UserSummary sender = userSummaryQueryRepository.findByUserId(message.getSenderId());
    UserSummary receiver = userSummaryQueryRepository.findByUserId(message.getReceiverId());
    return directMessageMapper.toDto(message, sender, receiver);
  }

  private CursorPageResponse<DirectMessageDto> toDtoPage(CursorPageResponse<DirectMessage> page) {
    List<DirectMessage> messages = page.data();

    Map<UUID, UserSummary> summaryMap = messages.isEmpty() ? Map.of() :
        userSummaryQueryRepository.findByUserIds(
                messages.stream()
                    .flatMap(m -> Stream.of(m.getSenderId(), m.getReceiverId()))
                    .distinct().toList()
            ).stream()
            .collect(Collectors.toMap(UserSummary::userId, Function.identity()));

    List<DirectMessageDto> data = messages.stream()
        .map(m -> directMessageMapper.toDto(m,
            summaryMap.get(m.getSenderId()),
            summaryMap.get(m.getReceiverId())))
        .toList();

    return new CursorPageResponse<>(data, page.nextCursor(), page.nextIdAfter(),
        page.hasNext(), page.totalCount(), page.sortBy(), page.sortDirection());
  }
}