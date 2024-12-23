import React from 'react';
import { AgentActiveThread, GetServerMap } from '@pinpoint-fe/constants';
import { useActiveThread } from './useActiveThread';
import { AgentActiveThreadView } from './AgentActiveThreadView';
import { useAtomValue } from 'jotai';
import { serverMapCurrentTargetAtom, serverMapCurrentTargetDataAtom } from '@pinpoint-fe/ui/atoms';
import { AgentActiveThreadSkeleton } from './AgentActiveThreadSkeleton';

export interface ActiveRequestProps {}

export const AgentActiveThreadFetcher = () => {
  const wsRef = React.useRef<WebSocket>();
  const [webSocketState, setWebSocketState] = React.useState<number>(WebSocket.CLOSED);
  const currentServerMapTarget = useAtomValue(serverMapCurrentTargetAtom);
  const applicationNameRef = React.useRef('');
  const applicationName = currentServerMapTarget?.applicationName || '';
  const { activeThreadCountsWithTotal, setActiveThreadCounts } = useActiveThread();
  const [isApplicationLocked, setApplicationLock] = React.useState(true);
  const currentTargetData = useAtomValue(serverMapCurrentTargetDataAtom) as GetServerMap.NodeData;

  React.useEffect(() => {
    initWebSocket();

    return () => {
      close();
    };
  }, []);

  React.useEffect(() => {
    if ((applicationName && !isApplicationLocked) || applicationNameRef.current === '') {
      applicationNameRef.current = applicationName;
    }
  }, [applicationName, isApplicationLocked]);

  React.useEffect(() => {
    const isSocketOpen = wsRef.current?.readyState === WebSocket.OPEN;

    if (applicationNameRef.current && isSocketOpen) {
      sendMessage({
        type: 'REQUEST',
        command: 'activeThreadCount',
        parameters: {
          applicationName: applicationNameRef.current,
        },
      });
    }
  }, [applicationNameRef.current, wsRef.current?.readyState]);

  const initWebSocket = () => {
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
        // setSocketOpen(true);
      },
      { signal },
    );
    ws.addEventListener(
      'message',
      (message) => {
        const parsedMessage = parseMessage(message.data);
        if (parsedMessage?.type === 'PING') {
          sendMessage({ type: 'PONG' });
        } else if (parsedMessage?.result) {
          setWebSocketState(WebSocket.OPEN);
          setActiveThreadCounts(parsedMessage);
        }
      },
      { signal },
    );
    ws.addEventListener(
      'close',
      () => {
        setWebSocketState(WebSocket.CLOSED);
        eventController.abort();
        initWebSocket();
        // wsRef.current = undefined;
      },
      { signal },
    );
  };

  const parseMessage = (message: string): AgentActiveThread.Response => {
    return JSON.parse(message);
  };

  const sendMessage = (message: AgentActiveThread.Request) => {
    wsRef.current?.send(JSON.stringify(message));
  };

  const close = () => {
    wsRef.current?.close();
  };

  return (
    <div className="w-full h-full">
      {webSocketState === WebSocket.OPEN ? (
        isApplicationLocked ? (
          <AgentActiveThreadView
            applicationLocked={isApplicationLocked}
            applicationName={applicationNameRef.current}
            thread={activeThreadCountsWithTotal}
            onClickLockButton={() => {
              setApplicationLock(!isApplicationLocked);
            }}
          />
        ) : currentTargetData?.isWas ? (
          <AgentActiveThreadView
            applicationLocked={isApplicationLocked}
            applicationName={applicationNameRef.current}
            thread={activeThreadCountsWithTotal}
            onClickLockButton={() => {
              setApplicationLock(!isApplicationLocked);
            }}
          />
        ) : (
          <div className="flex items-center justify-center w-full h-full">
            Selected target is not a WAS.
          </div>
        )
      ) : webSocketState === WebSocket.CONNECTING ? (
        <AgentActiveThreadSkeleton />
      ) : (
        <div className="flex items-center justify-center w-full h-full">Connection closed.</div>
      )}
    </div>
  );
};
