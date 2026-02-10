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
import { formatInTimeZone } from 'date-fns-tz';
import { BsGearFill } from 'react-icons/bs';
import { AgentActiveSetting, AgentActiveSettingType, DefaultValue } from './AgentActiveSetting';
import { HelpPopover } from '@pinpoint-fe/ui/src/components/HelpPopover';
import { useTimezone } from '@pinpoint-fe/ui/src/hooks';

export interface ActiveRequestProps {}

export const AgentActiveThreadFetcher = () => {
  const wsRef = React.useRef<WebSocket | undefined>(undefined);
  const [timezone] = useTimezone();
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
    try {
      return JSON.parse(message);
    } catch (error) {
      console.error('Error parsing message:', error);
      return {};
    }
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
        isApplicationLocked ||
        currentTargetData?.nodeCategory === GetServerMap.NodeCategory.SERVER ? (
          <div className="flex flex-col items-center p-4 h-full">
            <div className="flex flex-row gap-1 justify-between items-center p-1 w-full text-sm font-semibold truncate">
              <TooltipProvider>
                <Tooltip>
                  <TooltipTrigger asChild>
                    <Button
                      className="px-3 h-7 text-lg"
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
              <div className="flex flex-row gap-1 w-full truncate">
                {applicationNameRef.current}
                <HelpPopover helpKey="HELP_VIEWER.REAL_TIME" />
              </div>
              <div className="flex gap-1 items-center font-normal text-gray-400">
                <span className="text-sm">
                  {formatInTimeZone(
                    activeThreadCounts?.result?.timeStamp || 0,
                    timezone,
                    'yyyy.MM.dd HH:mm:ss',
                  )}
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
                  className={`flex absolute z-10 justify-center items-center w-[-webkit-fill-available] h-[-webkit-fill-available]`}
                >
                  <AgentActiveSetting
                    className="z-10"
                    defaultValues={setting}
                    onClose={() => setShowSetting(false)}
                    onApply={(newSetting) => {
                      setSetting(newSetting);
                    }}
                  />
                  <div className="absolute w-full h-full opacity-80 bg-background"></div>
                </div>
              )}
            </div>
          </div>
        ) : (
          <div className="flex justify-center items-center w-full h-full">
            Selected target is not a WAS.
          </div>
        )
      ) : webSocketState === WebSocket.CONNECTING ? (
        <AgentActiveThreadSkeleton />
      ) : (
        <div className="flex justify-center items-center w-full h-full">Connection closed.</div>
      )}
    </div>
  );
};
