import React from 'react';
import { useTranslation } from 'react-i18next';
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

export interface AgentActiveThreadFetcherProps {
  /**
   * 조회 대상 노드가 소속된 service.
   *
   * servicemap은 다른 service의 노드도 함께 그리므로, 조회는 화면의 service가 아니라 고른
   * 노드의 service로 나가야 한다. 값은 `useServerMapTargetServiceName`에서 오고, 그 훅이
   * `enableServiceMap`이 꺼져 있으면 undefined를 돌려주므로 설정이 꺼진 저장소에서는 항상
   * undefined다.
   */
  serviceName?: string;
}

/**
 * activeThreadCount 요청의 조회 대상.
 *
 * 세 값을 한 객체로 묶어 둔다. applicationName만 따로 고정하면 잠금(lock) 상태에서 이름은
 * 그대로인데 service만 새로 고른 노드를 따라가, 그 service에 없는 application을 묻게 된다.
 */
type ActiveThreadTarget = {
  applicationName: string;
  serviceName?: string;
  serviceType?: string;
};

export const AgentActiveThreadFetcher = ({ serviceName }: AgentActiveThreadFetcherProps) => {
  const { t } = useTranslation();
  const wsRef = React.useRef<WebSocket | undefined>(undefined);
  const [timezone] = useTimezone();
  const [webSocketState, setWebSocketState] = React.useState<number>(WebSocket.CLOSED);
  const currentServerMapTarget = useAtomValue(serverMapCurrentTargetAtom);
  const targetRef = React.useRef<ActiveThreadTarget>({ applicationName: '' });
  const applicationName = currentServerMapTarget?.applicationName || '';
  const serviceType = currentServerMapTarget?.serviceType;
  // const { activeThreadCountsWithTotal, setActiveThreadCounts } = useActiveThread();
  const [activeThreadCounts, setActiveThreadCounts] = React.useState<AgentActiveThread.Response>();
  const [isApplicationLocked, setApplicationLock] = React.useState(true);
  const currentTargetData = useAtomValue(serverMapCurrentTargetDataAtom) as GetServerMap.NodeData;
  const [showSetting, setShowSetting] = React.useState(false);
  const [setting, setSetting] = React.useState<AgentActiveSettingType>(DefaultValue);
  /**
   * 조회할 대상이 정해졌는지 여부.
   *
   * 대상이 없으면 activeThreadCount 요청을 보내지 않으므로 응답도 오지 않는다. 그러면 연결
   * 상태가 CONNECTING에 머물러 로딩 스켈레톤이 영영 떠 있게 되므로, 로딩 대신 무엇을 해야
   * 하는지 알려준다. (기준 application이 없는 servicemap 실시간 보기에서 아직 노드를 고르지
   * 않은 상태가 여기에 해당한다.)
   */
  const hasTarget = !!(applicationName || targetRef.current.applicationName);

  React.useEffect(() => {
    initWebSocket();

    return () => {
      close();
    };
  }, []);

  React.useEffect(() => {
    if ((applicationName && !isApplicationLocked) || targetRef.current.applicationName === '') {
      // 세 값을 한 번에 갈아 끼운다. 아래 effect는 객체가 아니라 값 하나하나를 비교하므로,
      // 핀만 껐다 켠 경우처럼 내용이 그대로면 요청을 다시 보내지 않는다.
      targetRef.current = { applicationName, serviceName, serviceType };
    }
  }, [applicationName, serviceName, serviceType, isApplicationLocked]);

  React.useEffect(() => {
    const isSocketOpen = wsRef.current?.readyState === WebSocket.OPEN;
    const target = targetRef.current;

    if (target.applicationName && isSocketOpen) {
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
  }, [
    targetRef.current.applicationName,
    targetRef.current.serviceName,
    targetRef.current.serviceType,
    wsRef.current?.readyState,
  ]);

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
      {!hasTarget ? (
        <div className="flex justify-center items-center w-full h-full text-muted-foreground">
          {t('SERVER_MAP.REAL_TIME.SELECT_NODE')}
        </div>
      ) : webSocketState === WebSocket.OPEN ? (
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
                    <p>
                      {isApplicationLocked
                        ? t('SERVER_MAP.REAL_TIME.UNLOCK_SERVER')
                        : t('SERVER_MAP.REAL_TIME.LOCK_SERVER')}
                    </p>
                  </TooltipContent>
                </Tooltip>
              </TooltipProvider>
              <div className="flex flex-row gap-1 w-full truncate">
                {targetRef.current.applicationName}
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
                applicationName={targetRef.current.applicationName}
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
            {t('SERVER_MAP.REAL_TIME.NOT_WAS')}
          </div>
        )
      ) : webSocketState === WebSocket.CONNECTING ? (
        <AgentActiveThreadSkeleton />
      ) : (
        <div className="flex justify-center items-center w-full h-full">
          {t('SERVER_MAP.REAL_TIME.CONNECTION_CLOSED')}
        </div>
      )}
    </div>
  );
};
