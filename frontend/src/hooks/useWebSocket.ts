import { Client, IMessage } from "@stomp/stompjs";
import { useCallback, useEffect, useState } from "react";
import { createStompClient, sendMessage, subscribeToTopic } from "../service/stompService";

/**
 * WebSocket 토픽 정의
 * 각 도메인별(ROOM, CHAT, GAME)로 토픽을 구분하고 roomId를 파라미터로 받아 동적 토픽 생성
 * 백엔드와 협의 후 변경될 수 있음
 */
export const SOCKET_TOPICS = {
  ROOM: (roomId: string) => `/room/room/${roomId}`,
  CHAT: (roomId: string) => `/topic/chat/${roomId}`,
  GAME: (roomId: string) => `/topic/game/${roomId}`,
};

/**
 * WebSocket 메시지 타입 정의
 * 각 도메인별 가능한 액션 타입을 상수로 정의
 * 백엔드와 협의 후 변경될 수 있음
 */
export const MESSAGE_TYPES = {
  ROOM: {
    KICK: "KICK", // 방에서 유저 강퇴
    JOIN: "JOIN", // 방 입장
    LEAVE: "LEAVE", // 방 퇴장
    UPDATE: "UPDATE", // 방 정보 업데이트
    READY: "READY", // 준비 상태 변경
    START: "START", // 게임 시작
  },
  GAME: {
    STATUS: {
      ATTACK: "ATTACK", // 공격 액션
      DAMAGE: "DAMAGE", // 데미지 받음
      DEAD: "DEAD", // 캐릭터 사망
      END: "END", // 게임 종료
    },
    PROBLEM: "PROBLEM", // 문제 전송/수신
    ANSWER: "ANSWER", // 답변 제출/결과
  },
  CHAT: {
    SEND: "SEND", // 메시지 전송
    RECEIVE: "RECEIVE", // 메시지 수신
  },
};

/**
 * WebSocket 연결 및 관리를 위한 React 훅
 *
 * 주요 기능:
 * - WebSocket 클라이언트 생성 및 연결 관리
 * - 토픽 구독 및 구독 해제
 * - 메시지 전송
 *
 * @returns WebSocket 관련 상태 및 함수들
 */
export const useWebSocket = () => {
  // WebSocket 클라이언트 상태
  const [client, setClient] = useState<Client | null>(null);
  // 연결 상태
  const [connected, setConnected] = useState(false);
  // 활성 구독 목록 (토픽별로 관리)
  const [subscriptions, setSubscripitons] = useState<Record<string, any>>({});

  // 컴포넌트 마운트 시 WebSocket 클라이언트 초기화
  useEffect(() => {
    // stompService에서 STOMP 클라이언트 생성
    const stompClient = createStompClient();

    // 연결 성공 이벤트 핸들러
    stompClient.onConnect = () => {
      setConnected(true);
    };

    // 연결 해제 이벤트 핸들러
    stompClient.onDisconnect = () => {
      setConnected(false);
    };

    // 클라이언트 활성화 (연결 시작)
    stompClient.activate();
    setClient(stompClient);

    // 컴포넌트 언마운트 시 정리 함수
    return () => {
      if (stompClient.active) {
        stompClient.deactivate();
      }
    };
  }, []);

  /**
   * 토픽 구독 함수
   *
   * @param topic 구독할 토픽 경로
   * @param callback 메시지 수신 시 실행할 콜백 함수
   * @returns 구독 객체 또는 실패 시 null
   */
  const subscribe = useCallback(
    (topic: string, callback: (message: IMessage) => void) => {
      // 클라이언트가 없거나 연결되지 않은 경우
      if (!client || !connected) return null;

      // 토픽 구독 수행
      const subscribtion = subscribeToTopic(client, topic, callback);

      // 구독 성공 시 상태에 추가
      if (subscribtion) {
        setSubscripitons((prev) => ({
          ...prev,
          [topic]: subscribtion,
        }));
      }

      return subscribtion;
    },
    [client, connected]
  );

  /**
   * 토픽 구독 해제 함수
   *
   * @param topic 구독 해제할 토픽 경로
   */
  const unsubscribe = useCallback(
    (topic: string) => {
      if (subscriptions[topic]) {
        // 구독 객체 해제
        subscriptions[topic].unsubscribe();
        // 상태에서 제거
        setSubscripitons((prev) => {
          const newSubs = { ...prev };
          delete newSubs[topic];
          return newSubs;
        });
      }
    },
    [subscriptions]
  );

  /**
   * 메시지 전송 함수
   *
   * @param destination 메시지를 전송할 대상 경로
   * @param body 전송할 메시지 본문 (객체)
   * @param headers 추가 헤더 (선택적)
   * @returns 전송 성공 여부
   */
  const send = useCallback(
    (destination: string, body: any, headers = {}) => {
      // 클라이언트가 없거나 연결되지 않은 경우
      if (!client || !connected) return false;

      // 메시지 전송
      sendMessage(client, destination, body, headers);
      return true;
    },
    [client, connected]
  );

  // 훅에서 제공하는 기능과 상태 반환
  return {
    client, // STOMP 클라이언트 객체
    connected, // 연결 상태
    subscribe, // 구독 함수
    unsubscribe, // 구독 해제 함수
    send, // 메시지 전송 함수
    topics: SOCKET_TOPICS, // 토픽 정의 객체
    messageTypes: MESSAGE_TYPES, // 메시지 타입 정의 객체
  };
};
