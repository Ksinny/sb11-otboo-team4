package com.sprint.mission.otboo.security.interceptor;

import com.sprint.mission.otboo.security.token.provider.TokenProvider;
import com.sprint.mission.otboo.security.usersession.registry.UserSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

  private final TokenProvider tokenProvider;
  private final UserSessionRegistry userSessionRegistry;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    return message;
  }
}