import React from 'react';
import { AgentActiveThread, GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { AgentActiveThreadView } from './AgentActiveThreadView';
import { useAtomValue } from 'jotai';
import {
  serverMapCurrentTargetAtom,
  serverMapCurrentTargetDataAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { AgentActiveThreadSkeleton } from './AgentActiveThreadSkeleton';
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '../../components/ui/tooltip';
import { Button } from '../../components/ui/button';
import { RxDrawingPinFilled, RxDrawingPin } from 'react-icons/rx';
import { format } from 'date-fns';
import { BsGearFill } from 'react-icons/bs';
import { AgentActiveSetting, AgentActiveSettingType, DefaultValue } from './AgentActiveSetting';
import { HelpPopover } from '@pinpoint-fe/ui/src/components/HelpPopover';

export interface ActiveRequestProps {}

export const AgentActiveThreadFetcher = () => {
  const wsRef = React.useRef<WebSocket>();
  const [webSocketState, setWebSocketState] = React.useState<number>(WebSocket.CLOSED);
  const currentServerMapTarget = useAtomValue(serverMapCurrentTargetAtom);
  const applicationNameRef = React.useRef('');
  const applicationName = currentServerMapTarget?.applicationName || '';
  // const { activeThreadCountsWithTotal, setActiveThreadCounts } = useActiveThread();
  const [activeThreadCounts, setActiveThreadCounts] = React.useState<AgentActiveThread.Response>();
  const [isApplicationLocked, setApplicationLock] = React.useState(true);
  const currentTargetData = useAtomValue(serverMapCurrentTargetDataAtom) as GetServerMap.NodeData;
  const [showSetting, setShowSetting] = React.useState(false);
  const [setting, setSetting] = React.useState<AgentActiveSettingType>(DefaultValue);

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
        isApplicationLocked || currentTargetData?.isWas ? (
          <div className="flex flex-col items-center h-full p-4">
            <div className="flex flex-row items-center justify-between w-full gap-1 p-1 text-sm font-semibold truncate">
              <TooltipProvider>
                <Tooltip>
                  <TooltipTrigger asChild>
                    <Button
                      className="px-3 text-lg h-7"
                      variant="ghost"
                      onClick={() => setApplicationLock(!isApplicationLocked)}
                    >
                      {isApplicationLocked ? <RxDrawingPinFilled /> : <RxDrawingPin />}
                    </Button>
                  </TooltipTrigger>
                  <TooltipContent side="left">
                    <p>{isApplicationLocked ? 'Unlock current server' : 'Lock current server'}</p>
                  </TooltipContent>
                </Tooltip>
              </TooltipProvider>
              <div className="flex flex-row w-full gap-1 truncate">
                {applicationNameRef.current}
                <HelpPopover helpKey="HELP_VIEWER.REAL_TIME" />
              </div>
              <div className="flex items-center gap-1 font-normal text-gray-400">
                <span className="text-sm">
                  {format(activeThreadCounts?.result?.timeStamp || 0, 'yyyy.MM.dd HH:mm:ss')}
                </span>
                <BsGearFill
                  className="text-base cursor-pointer"
                  onClick={() => setShowSetting(true)}
                />
              </div>
            </div>
            <div className="flex flex-grow w-full h-[-webkit-fill-available] overflow-hidden">
              <AgentActiveThreadView
                applicationName={applicationNameRef.current}
                activeThreadCounts={activeThreadCounts?.result}
                setting={setting}
              />
              {showSetting && (
                <div
                  className={`absolute w-[-webkit-fill-available] h-[-webkit-fill-available] z-10 flex items-center justify-center`}
                >
                  <AgentActiveSetting
                    className="z-10"
                    defaultValues={setting}
                    onClose={() => setShowSetting(false)}
                    onApply={(newSetting) => {
                      setSetting(newSetting);
                    }}
                  />
                  <div className="absolute w-full h-full bg-background opacity-80"></div>
                </div>
              )}
            </div>
          </div>
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
