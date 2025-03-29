package com.finbattle.global.common.handler;

import com.finbattle.domain.chat.model.StompPrincipal;
import com.finbattle.domain.member.dto.AuthenticUser;
import com.finbattle.domain.oauth.dto.AuthenticatedUser;
import com.finbattle.global.common.Util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JWTUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    Long memberId = jwtUtil.getAccessMemberId(token);
                    String providerId = jwtUtil.getAccessProviderId(token);

                    AuthenticatedUser user = new AuthenticatedUser(
                        AuthenticUser.builder()
                            .memberId(memberId)
                            .providerId(providerId)
                            .build()
                    );

                    accessor.setUser(new StompPrincipal(memberId)); // ✅ Principal 등록
                    log.info("✅ STOMP CONNECT 인증 성공: {}", memberId);
                } catch (Exception e) {
                    log.warn("❌ JWT 인증 실패: {}", e.getMessage());
                    throw new IllegalArgumentException("Invalid WebSocket token");
                }
            } else {
                throw new IllegalArgumentException("Missing Authorization Header");
            }
        }

        return message;
    }
}
