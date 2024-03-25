// import '@pinpoint-fe/scatter-chart/dist/index.css';
import React from 'react';
import { AgentActiveThread, GetServerMap } from '@pinpoint-fe/constants';
import { useActiveThread } from './useActiveThread';
import { AgentActiveThreadView } from './AgentActiveThreadView';

export interface ActiveRequestProps {
  nodeData?: GetServerMap.NodeData;
}

export const AgentActiveThreadFetcher = ({ nodeData }: ActiveRequestProps) => {
  const wsRef = React.useRef<WebSocket>();
  const applicationName = nodeData?.applicationName;
  const { activeThreadCountsWithTotal, setActiveThreadCounts } = useActiveThread();

  React.useEffect(() => {
    initWebSocket();

    return () => {
      close();
    };
  }, []);

  React.useEffect(() => {
    const isSocketOpen = wsRef.current?.readyState === wsRef.current?.OPEN;

    if (applicationName && isSocketOpen) {
      sendMessage({
        type: 'REQUEST',
        command: 'activeThreadCount',
        parameters: {
          applicationName,
        },
      });
    }
  }, [applicationName, wsRef.current?.readyState]);

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
          setActiveThreadCounts(parsedMessage);
        }
      },
      { signal },
    );
    ws.addEventListener(
      'close',
      () => {
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
      <AgentActiveThreadView
        applicationName={applicationName}
        thread={activeThreadCountsWithTotal}
      />
    </div>
  );
};
