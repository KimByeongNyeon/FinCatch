package com.finbattle.domain.room.controller;

import com.finbattle.domain.oauth.dto.AuthenticatedUser;
import com.finbattle.domain.room.dto.RoomCreateRequest;
import com.finbattle.domain.room.dto.RoomResponse;
import com.finbattle.domain.room.dto.RoomType;
import com.finbattle.domain.room.service.RoomService;
import com.finbattle.domain.room.service.RoomSubscriptionService;
import com.finbattle.global.common.model.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Room API", description = "방 관련 기능을 제공하는 컨트롤러")
public class RoomController {

    private final RoomService roomService;
    private final RoomSubscriptionService roomSubscriptionService;

    /**
     * 방 생성
     */
    @Operation(summary = "방 생성하기", description = "방 생성 api")
    @PostMapping
    public ResponseEntity<BaseResponse<RoomResponse>> createRoom(
        @AuthenticationPrincipal AuthenticatedUser detail,
        @RequestBody RoomCreateRequest request) {
        RoomResponse response = roomService.createRoom(detail.getMemberId(), request);
        roomSubscriptionService.createRoomSubscription(response);
        return ResponseEntity.ok(new BaseResponse<>(response));
    }

    /**
     * 방 삭제
     */
    @Operation(summary = "방 삭제하기", description = "방 삭제 api")
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        roomSubscriptionService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 방 목록 조회
     */
    @Operation(summary = "방 모든 방 조회하기", description = "방 전체 조회 api")
    @GetMapping
    public ResponseEntity<BaseResponse<List<RoomResponse>>> getAllRooms() {
        return ResponseEntity.ok(new BaseResponse<>(roomService.getAllRooms()));
    }

    /**
     * 주제별 방 조회
     */
    @Operation(summary = "방 주제별 방 조회하기", description = "방 주제별 방 조회 api")
    @GetMapping("/type/{roomType}")
    public ResponseEntity<BaseResponse<List<RoomResponse>>> getRoomsByType(
        @PathVariable String roomType) {
        return ResponseEntity.ok(new BaseResponse<>(
            roomService.getRoomsByType(RoomType.valueOf(roomType.toUpperCase()))));
    }

    /**
     * 특정 방 조회
     */
    @Operation(summary = "방 id 검색 조회", description = "방 id 검색 조회 api")
    @GetMapping("/{roomId}")
    public ResponseEntity<BaseResponse<RoomResponse>> getRoomById(@PathVariable Long roomId) {
        return ResponseEntity.ok(new BaseResponse<>(roomService.getRoomById(roomId)));
    }

    /**
     * OPEN 상태의 방 목록 조회
     */
    @Operation(summary = "방 열려 있는 방 조회하기", description = "방 열려 있는 방 조회 api")
    @GetMapping("/open")
    public ResponseEntity<BaseResponse<List<RoomResponse>>> getOpenRooms() {
        return ResponseEntity.ok(new BaseResponse<>(roomService.getOpenRooms()));
    }


    @Operation(summary = "게임 시작하기", description = "room 게임 시작 api")
    @PutMapping("/start/{roomId}")
    public ResponseEntity<BaseResponse<Void>> readyRoom(@PathVariable Long roomId,
        @AuthenticationPrincipal AuthenticatedUser detail) {
        roomService.startRoom(roomId, detail.getMemberId());
        return ResponseEntity.ok(new BaseResponse<>());
    }

    /**
     * 방 정보 요청 -> RedisRoom 정보 조회
     */
    @Operation(summary = "대기방 정보 가지고 오기", description = "대기방 정보 api")
    @PostMapping("/room/{roomId}/info")
    public ResponseEntity<BaseResponse<Void>> getRoomInfo(@PathVariable Long roomId) {
        log.info("Requesting info for room {}", roomId);
        roomSubscriptionService.getRoomInfo(roomId);
        return ResponseEntity.ok(new BaseResponse<>());
    }


    /**
     * 방 참가 요청 처리 (RDB에 저장하지 않고 Redis에서만 관리)
     */
    @Operation(summary = "대기방 들어가기", description = "대기방 참가 api")
    @PostMapping("/room/{roomId}/join")
    public ResponseEntity<BaseResponse<Void>> joinRoom(@PathVariable Long roomId,
        @AuthenticationPrincipal AuthenticatedUser detail) {
        log.info("User {} joining room {}", detail.getMemberId(), roomId);
        roomSubscriptionService.joinRoom(roomId, detail.getMemberId());
        return ResponseEntity.ok(new BaseResponse<>());
    }

    /**
     * 방 구독자 수 확인
     */
    @Operation(summary = "대기방 인원수 확인", description = "대기방 인원수 확인 api")
    @PostMapping("/room/{roomId}/count")
    public ResponseEntity<BaseResponse<Void>> getRoomUserCount(@PathVariable Long roomId) {
        log.info("Checking user count for room {}", roomId);
        roomSubscriptionService.getRoomUserCount(roomId);
        return ResponseEntity.ok(new BaseResponse<>());
    }

    /**
     * 유저가 방을 나갈 때 처리 (방장이면 위임 or 방 해체)
     */
    @Operation(summary = "대기방에서 나가기", description = "대기방에서 나가기 api")
    @PostMapping("/room/{roomId}/leave")
    public ResponseEntity<BaseResponse<Void>> leaveRoom(@PathVariable Long roomId,
        @AuthenticationPrincipal AuthenticatedUser detail) {
        log.info("User {} leaving room {}", detail.getMemberId(), roomId);
        roomSubscriptionService.leaveRoom(roomId, detail.getMemberId());
        return ResponseEntity.ok(new BaseResponse<>());
    }

    /**
     * 강퇴(kick) 요청 처리
     */
    @Operation(summary = "대기방에서 강퇴 시키기", description = "대기방 강퇴 api")
    @PostMapping("/room/{roomId}/kick/{targetUserId}")
    public ResponseEntity<BaseResponse<Void>> kickUser(@PathVariable Long roomId,
        @AuthenticationPrincipal AuthenticatedUser detail,
        @PathVariable Long targetUserId) {
        log.info("host {} Kicking user {} from room {}", detail.getMemberId(), targetUserId,
            roomId);
        roomSubscriptionService.kickUser(roomId, detail.getMemberId(), targetUserId);
        return ResponseEntity.ok(new BaseResponse<>());
    }

    /**
     * 준비(ready) 요청 처리
     */
    @Operation(summary = "대기방 준비 상태로 변환", description = "대기방 준비 상태로 변환 api")
    @PostMapping("/room/{roomId}/ready")
    public ResponseEntity<BaseResponse<Void>> setReady(@PathVariable Long roomId,
        @AuthenticationPrincipal AuthenticatedUser detail) {
        log.info("User {} set READY in room {}", detail.getMemberId(), roomId);
        roomSubscriptionService.setUserReady(roomId, detail.getMemberId());
        return ResponseEntity.ok(new BaseResponse<>());
    }

    /**
     * 준비 해제
     */
    @Operation(summary = "대기방 준비 해제", description = "대기방 준비 해제 api")
    @PostMapping("/room/{roomId}/unready")
    public ResponseEntity<BaseResponse<Void>> setUnready(@PathVariable Long roomId,
        @AuthenticationPrincipal AuthenticatedUser detail) {
        log.info("User {} set UNREADY in room {}", detail.getMemberId(), roomId);
        roomSubscriptionService.setUserUnReady(roomId, detail.getMemberId());
        return ResponseEntity.ok(new BaseResponse<>());
    }
}
