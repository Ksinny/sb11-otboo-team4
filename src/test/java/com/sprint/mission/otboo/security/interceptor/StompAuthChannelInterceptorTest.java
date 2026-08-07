package com.sprint.mission.otboo.security.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sprint.mission.otboo.security.details.UserPrincipal;
import com.sprint.mission.otboo.security.token.dto.AccessTokenClaims;
import com.sprint.mission.otboo.security.token.provider.TokenProvider;
import com.sprint.mission.otboo.security.usersession.registry.UserSessionRegistry;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
@DisplayName("StompAuthChannelInterceptor")
class StompAuthChannelInterceptorTest {

  @InjectMocks
  private StompAuthChannelInterceptor interceptor;

  @Mock
  private TokenProvider tokenProvider;

  @Mock
  private UserSessionRegistry userSessionRegistry;

  private Message<byte[]> connectMessage(String authorizationHeader) {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
    if (authorizationHeader != null) {
      accessor.setNativeHeader("Authorization", authorizationHeader);
    }
    accessor.setLeaveMutable(true);
    return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
  }

  @Nested
  @DisplayName("CONNECT 인증")
  class ConnectAuthentication {

    @Test
    @DisplayName("유효한 토큰이면 인증 사용자를 STOMP 세션에 설정한다")
    void 유효한_토큰이면_인증_사용자를_STOMP_세션에_설정한다() {
      // given
      UUID userId = UUID.randomUUID();
      UUID sessionId = UUID.randomUUID();
      given(tokenProvider.parseAccessToken("valid-token"))
          .willReturn(new AccessTokenClaims(userId, sessionId, "USER"));
      Message<byte[]> message = connectMessage("Bearer valid-token");

      // when
      Message<?> result = interceptor.preSend(message, null);

      // then
      StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
      Authentication authentication = (Authentication) accessor.getUser();
      assertThat(authentication).isNotNull();
      assertThat(authentication.getPrincipal()).isEqualTo(new UserPrincipal(userId, "USER"));
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증을 시도하지 않는다")
    void Authorization_헤더가_없으면_인증을_시도하지_않는다() {
      // given
      Message<byte[]> message = connectMessage(null);

      // when
      Message<?> result = interceptor.preSend(message, null);

      // then
      StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
      assertThat(accessor.getUser()).isNull();
      verify(tokenProvider, never()).parseAccessToken(any());
    }
  }
}