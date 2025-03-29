package com.finbattle.domain.room.service;

import static com.finbattle.global.common.model.dto.BaseResponseStatus.MEMBER_NOT_FOUND;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finbattle.domain.member.model.Member;
import com.finbattle.domain.member.repository.MemberRepository;
import com.finbattle.domain.room.dto.EventMessage;
import com.finbattle.domain.room.dto.FailResponse;
import com.finbattle.domain.room.dto.MessageType;
import com.finbattle.domain.room.dto.RedisRoomMember;
import com.finbattle.domain.room.dto.RoomResponse;
import com.finbattle.domain.room.dto.RoomStatus;
import com.finbattle.domain.room.model.RedisRoom;
import com.finbattle.domain.room.repository.RedisRoomRepository;
import com.finbattle.global.common.exception.exception.BusinessException;
import com.finbattle.global.common.redis.RedisPublisher;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomSubscriptionService {

    private final RedisRoomRepository redisRoomRepository;
    private final RedisPublisher redisPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MemberRepository memberRepository;

    /**
     * 방을 생성하고 Redis에 저장 후 이벤트 발행
     */
    public void createRoomSubscription(RoomResponse response) {
        RedisRoom redisRoom = new RedisRoom();
        redisRoom.setRoomId(response.getRoomId());
        redisRoom.setMaxPeople(2);
        redisRoom.setStatus(RoomStatus.OPEN);

        Member member = memberRepository.findByMemberId(response.getMemberId())
            .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        RedisRoomMember host = new RedisRoomMember();
        host.setMemberId(member.getMemberId());
        host.setMainCat(member.getMainCat());
        host.setNickname(member.getNickname());
        host.setStatus("READY");

        redisRoom.setHost(host);
        redisRoom.getMembers().add(host);

        redisRoomRepository.save(redisRoom);

        publishEvent(MessageType.CREATE, response.getRoomId(), redisRoom);
    }

    /**
     * 특정 방의 현재 참가자 수 조회 후 이벤트 발행
     */
    public void getRoomUserCount(Long roomId) {
        RedisRoom redisRoom = getRedisRoom(roomId);
        int count = redisRoom == null ? 0 : redisRoom.getMembers().size();
        publishEvent(MessageType.COUNT, roomId, count);
    }

    /**
     * 방 참가 처리 후 Redis 저장 및 이벤트 발행
     */
    public void joinRoom(Long roomId, Long userId) {
        RedisRoom redisRoom = getRedisRoom(roomId);

        if (redisRoom.getStatus() != RoomStatus.OPEN) {
            publishEvent(MessageType.JOIN_FAIL, roomId, new FailResponse("방이 닫혀있습니다."));
            throw new IllegalStateException("방에 입장할 수 없는 상태입니다.");
        }

        if (redisRoom.getMembers().size() >= redisRoom.getMaxPeople()) {
            publishEvent(MessageType.JOIN_FAIL, roomId, new FailResponse("방 정원이 초과되었습니다."));
            throw new IllegalStateException("방 정원이 초과되었습니다.");
        }

        boolean alreadyIn = redisRoom.getMembers().stream()
            .anyMatch(m -> userId.equals(m.getMemberId()));
        if (alreadyIn) {
            publishEvent(MessageType.JOIN_FAIL, roomId, new FailResponse("이미 입장한 유저입니다."));
            throw new IllegalStateException("이미 방에 입장해 있습니다.");
        }

        Member m = memberRepository.findByMemberId(userId)
            .orElseThrow(() -> new BusinessException(MEMBER_NOT_FOUND));

        RedisRoomMember member = new RedisRoomMember();
        member.setMemberId(userId);
        member.setNickname(m.getNickname());
        member.setMainCat(m.getMainCat());
        member.setStatus("UNREADY");
        redisRoom.getMembers().add(member);

        redisRoomRepository.save(redisRoom);
        publishEvent(MessageType.READY, roomId, userId);
    }

    /**
     * 방에서 유저 제거 후 Redis 저장 및 이벤트 발행
     */
    public void leaveRoom(Long roomId, Long userId) {
        RedisRoom redisRoom = getRedisRoom(roomId);
        if (redisRoom == null) {
            // ✅ MessageType.DELETE 사용
            publishEvent(MessageType.DELETE, roomId, null);
            return;
        }

        // 1) 멤버 목록 수정
        List<RedisRoomMember> members = redisRoom.getMembers();
        members.removeIf(m -> m.getMemberId().equals(userId));

        // 2) 방 인원이 0명이면 방 삭제
        if (members.isEmpty()) {
            deleteRoom(roomId);
            return;
        }

        // 3) Redis에 다시 저장
        redisRoomRepository.save(redisRoom);

        // 4) 이벤트 발행
        // ✅ MessageType.LEAVE 사용
        publishEvent(MessageType.LEAVE, roomId, userId);
    }

    /**
     * 유저 강퇴 처리 후 Redis 저장 및 이벤트 발행
     */
    public void kickUser(Long roomId, Long hostId, Long targetUserId) {
        RedisRoom redisRoom = getRedisRoom(roomId);
        if (redisRoom == null) {
            // ✅ MessageType.KICK_FAIL 사용
            publishEvent(MessageType.KICK_FAIL, roomId, new FailResponse("방이 존재하지 않습니다."));
            throw new IllegalStateException("방이 존재하지 않습니다.");
        }

        if (hostId != redisRoom.getHost().getMemberId()) {
            publishEvent(MessageType.KICK_FAIL, roomId,
                new FailResponse("방장 권한을 가진 사람만 강퇴할 수 있습니다."));
            throw new IllegalStateException("방장 권한을 가진 사람만 강퇴할 수 있습니다.");
        }

        List<RedisRoomMember> members = redisRoom.getMembers();
        boolean removed = members.removeIf(m -> m.getMemberId().equals(targetUserId));
        if (!removed) {
            // ✅ MessageType.KICK_FAIL 사용
            publishEvent(MessageType.KICK_FAIL, roomId, new FailResponse("유저가 존재하지 않습니다."));
            throw new IllegalStateException("해당 유저가 방에 없습니다.");
        }

        // 기존 RoomContainer를 가져와서 members 갱신
        redisRoomRepository.save(redisRoom);

        // ✅ MessageType.KICK 사용
        publishEvent(MessageType.KICK, roomId, targetUserId);
    }

    /**
     * 방 삭제 및 이벤트 발행
     */
    public void deleteRoom(Long roomId) {
        redisRoomRepository.deleteById(roomId);
        publishEvent(MessageType.DELETE, roomId, null);
        log.info("Room {} deleted from Redis", roomId);
    }

    /**
     * 특정 방의 현재 참가자 수 조회 후 이벤트 발행
     */
    public void getRoomInfo(Long roomId) {
        RedisRoom redisRoom = getRedisRoom(roomId);
        if (redisRoom == null) {
            return;
        }
        publishEvent(MessageType.INFO, roomId, redisRoom);
    }

    /**
     * 유저 준비 상태 변경 후 Redis 저장 및 이벤트 발행
     */
    public void setUserReady(Long roomId, Long userId) {
        RedisRoom redisRoom = getRedisRoom(roomId);
        if (redisRoom == null) {
            throw new IllegalStateException("방이 존재하지 않습니다.");
        }

        List<RedisRoomMember> members = redisRoom.getMembers();
        RedisRoomMember member = members.stream()
            .filter(m -> m.getMemberId().equals(userId))
            .findFirst()
            .orElse(null);
        if (member == null) {
            throw new IllegalStateException("해당 유저는 방에 없습니다.");
        }

        member.setStatus("READY");

        // 변경된 멤버 목록을 다시 세팅
        redisRoomRepository.save(redisRoom);

        // ✅ MessageType.READY 사용
        publishEvent(MessageType.READY, roomId, redisRoom);
    }

    /**
     * Redis에서 방 정보를 가져오기
     */
    public RedisRoom getRedisRoom(Long roomId) {
        return redisRoomRepository.findById(roomId).orElse(null);
    }

    /**
     * Redis에서 방 유저 준비 해제
     */
    public void setUserUnReady(Long roomId, Long userId) {
        RedisRoom redisRoom = getRedisRoom(roomId);

        List<RedisRoomMember> members = redisRoom.getMembers();
        RedisRoomMember member = members.stream()
            .filter(m -> userId.equals(m.getMemberId()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("해당 유저는 방에 없습니다."));

        if ("READY".equals(member.getStatus())) {
            member.setStatus("UNREADY");
            redisRoomRepository.save(redisRoom);
            publishEvent(MessageType.UNREADY, roomId, redisRoom);
            log.info("✅ 유저 {}의 상태를 UNREADY 로 변경", userId);
        } else {
            log.info("ℹ️ 유저 {}는 이미 UNREADY 상태입니다.", userId);
        }
    }

    /**
     * 이벤트 메시지를 Redis Pub/Sub을 통해 발행
     */
    private void publishEvent(MessageType event, Long roomId, Object data) {
        try {
            EventMessage<Object> message = new EventMessage<>(event, roomId, data);
            String jsonMessage = objectMapper.writeValueAsString(message);
            redisPublisher.publish("room:" + roomId, jsonMessage);
            log.info("🚀 Published event: {} -> room:{}", event, roomId);
        } catch (JsonProcessingException e) {
            log.error("Failed to publish event: {}", e.getMessage());
        }
    }

}
