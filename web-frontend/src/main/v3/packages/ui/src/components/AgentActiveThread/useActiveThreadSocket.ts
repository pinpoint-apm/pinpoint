import React from 'react';
import { AgentActiveThread } from '@pinpoint-fe/ui/src/constants';
import { ActiveThreadTarget } from '@pinpoint-fe/ui/src/atoms';

/**
 * 끊긴 소켓을 다시 붙이기까지 기다리는 시간(ms). 시도할수록 늘리고 마지막 값에서 멈춘다.
 *
 * 서버가 요청을 거절하며 곧바로 끊는 경우가 있다(모르는 serviceName이면
 * `ActiveThreadCountHandler`가 BAD_DATA로 세션을 닫는다). 이때 지체 없이 다시 붙으면
 * 연결→요청→끊김이 쉼 없이 반복되며 서버와 브라우저를 함께 태운다.
 *
 * 간격을 되돌리는 기준은 "연결됐다"가 아니라 "응답이 왔다"이다. 거절당하는 세션도 open까지는
 * 정상으로 열리므로, open에서 되돌리면 간격이 1초에 묶인 채 영원히 반복된다.
 */
const RECONNECT_DELAYS_MS = [1000, 2000, 5000, 10000, 30000];

/**
 * activeThreadCount WebSocket 하나를 열고 대상의 수치를 받아 온다.
 *
 * 대상(`target`)이 바뀌면 그 대상으로 다시 요청하고, 끊기면 간격을 늘려 가며 다시 붙는다.
 * 화면(`AgentActiveThreadFetcher`)은 무엇을 조회할지만 정하고, 소켓의 생애는 여기서 끝난다.
 */
export const useActiveThreadSocket = (target?: ActiveThreadTarget) => {
  const wsRef = React.useRef<WebSocket | undefined>(undefined);
  const reconnectTimerRef = React.useRef<ReturnType<typeof setTimeout> | undefined>(undefined);
  const reconnectCountRef = React.useRef(0);
  // 화면을 떠났는지 여부. 떠날 때 부르는 close()도 close 이벤트를 일으키므로, 이 표시가 없으면
  // 사라진 화면이 소켓을 다시 열고 그 소켓은 아무도 닫지 않는다.
  const isUnmountedRef = React.useRef(false);
  const [webSocketState, setWebSocketState] = React.useState<number>(WebSocket.CLOSED);
  const [activeThreadCounts, setActiveThreadCounts] = React.useState<AgentActiveThread.Response>();

  React.useEffect(() => {
    isUnmountedRef.current = false;
    initWebSocket();

    return () => {
      isUnmountedRef.current = true;
      clearTimeout(reconnectTimerRef.current);
      close();
    };
  }, []);

  React.useEffect(() => {
    const isSocketOpen = wsRef.current?.readyState === WebSocket.OPEN;

    if (target?.applicationName && isSocketOpen) {
      sendMessage({
        type: 'REQUEST',
        command: 'activeThreadCount',
        parameters: {
          applicationName: target.applicationName,
          // 키 이름은 백엔드가 정한 것을 그대로 쓴다
          // (ActiveThreadCountHandler의 SERVICE_NAME_KEY / SERVICE_TYPE_NAME_KEY).
          // enableServiceMap이 꺼져 있으면 serviceName이 undefined로 들어오므로
          // (useServerMapTargetServiceName이 한 곳에서 막는다) 파라미터도 붙지 않는다.
          // 설정이 꺼진 저장소에는 지금까지와 똑같은 요청이 나간다(백엔드도 DEFAULT로 해석한다).
          ...(target.serviceName
            ? { serviceName: target.serviceName, serviceTypeName: target.serviceType }
            : {}),
        },
      });
    }
  }, [target, wsRef.current?.readyState]);

  function initWebSocket() {
    const location = window.location;
    const protocol = location.protocol.indexOf('https') === -1 ? 'ws' : 'wss';
    const url = `${protocol}://${location.host}/api/agent/activeThread`;
    const eventController = new AbortController();
    const { signal } = eventController;
    const ws = new WebSocket(url);
    wsRef.current = ws;

    ws.addEventListener(
      'open',
      () => {
        setWebSocketState(WebSocket.CONNECTING);
      },
      { signal },
    );
    ws.addEventListener('message', (message) => handleMessage(message.data), { signal });
    ws.addEventListener(
      'close',
      () => {
        eventController.abort();
        // 화면을 떠나며 부른 close()도 여기로 온다. 그때 다시 붙으면 사라진 화면의 소켓이
        // 남아 아무도 닫지 못한 채 재연결을 반복한다.
        if (isUnmountedRef.current) {
          return;
        }

        setWebSocketState(WebSocket.CLOSED);
        scheduleReconnect();
      },
      { signal },
    );
  }

  function handleMessage(data: string) {
    const parsedMessage = parseMessage(data);
    // 응답이 왔다는 것은 이 세션이 실제로 살아 있다는 뜻이다. 다음에 끊기면 처음 간격부터.
    reconnectCountRef.current = 0;

    if (parsedMessage?.type === 'PING') {
      sendMessage({ type: 'PONG' });
      return;
    }
    if (parsedMessage?.result) {
      setWebSocketState(WebSocket.OPEN);
      setActiveThreadCounts(parsedMessage);
    }
  }

  function scheduleReconnect() {
    const delay =
      RECONNECT_DELAYS_MS[Math.min(reconnectCountRef.current, RECONNECT_DELAYS_MS.length - 1)];
    reconnectCountRef.current += 1;
    reconnectTimerRef.current = setTimeout(() => {
      if (isUnmountedRef.current) {
        return;
      }
      initWebSocket();
    }, delay);
  }

  function parseMessage(message: string): AgentActiveThread.Response {
    try {
      return JSON.parse(message);
    } catch (error) {
      console.error('Error parsing message:', error);
      return {};
    }
  }

  function sendMessage(message: AgentActiveThread.Request) {
    wsRef.current?.send(JSON.stringify(message));
  }

  function close() {
    wsRef.current?.close();
  }

  return { webSocketState, activeThreadCounts };
};
