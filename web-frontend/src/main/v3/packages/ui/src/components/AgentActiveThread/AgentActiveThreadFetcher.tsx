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
import { useLocation } from 'react-router-dom';
import { useServiceNameForLink, useTimezone } from '@pinpoint-fe/ui/src/hooks';
import { getServiceNameFromPath, isServiceGroupTarget } from '@pinpoint-fe/ui/src/utils';

export interface ActiveRequestProps {
  /**
   * 조회 대상 노드가 소속된 service.
   *
   * servicemap 실시간 보기에서는 다른 service의 노드도 고를 수 있으므로, 조회는 화면의 service가
   * 아니라 고른 노드의 service로 나가야 한다. 넘기지 않으면 화면의 service로 조회한다.
   */
  serviceName?: string;
}

export const AgentActiveThreadFetcher = ({ serviceName }: ActiveRequestProps) => {
  const { t } = useTranslation();
  const wsRef = React.useRef<WebSocket | undefined>(undefined);
  /** 소켓을 유지해야 하는지. 의도적으로 닫을 때 자동 재연결을 막는다. */
  const shouldConnectRef = React.useRef(false);
  const [timezone] = useTimezone();
  // 대상이 정해진 뒤에 소켓을 열기 때문에, 첫 화면은 "연결 중"이 맞다. CLOSED로 두면 연결되기
  // 전 한 프레임 동안 "연결이 종료되었습니다"가 스친다.
  const [webSocketState, setWebSocketState] = React.useState<number>(WebSocket.CONNECTING);
  const currentServerMapTarget = useAtomValue(serverMapCurrentTargetAtom);
  /**
   * 소켓에 실어 보낼 조회 대상. 없으면 `applicationName`이 빈 문자열이다.
   *
   * 세 값을 한 객체에 모아 두는 이유는 applicationName·serviceType·serviceName이 반드시 한 벌로
   * 갈려야 하기 때문이다. 하나만 새 노드의 값으로 바뀌면 그 service에 없는 application을 묻는
   * 요청이 된다.
   *
   * ref가 아니라 state다. 소켓을 열고 닫는 것이 이 값에 달려 있어서, 값이 바뀌면 렌더가 반드시
   * 일어나야 한다. (ref로 두면 다른 이유로 렌더가 일어날 때까지 반영이 밀린다)
   */
  const [requestTarget, setRequestTarget] = React.useState<AgentActiveThread.RequestParameters>({
    applicationName: '',
  });
  // 소켓 이벤트 핸들러에서 최신 대상을 읽기 위한 거울.
  const requestTargetRef = React.useRef(requestTarget);
  requestTargetRef.current = requestTarget;
  /**
   * 직전에 고른 것이 조회할 수 있는 대상이었는지.
   *
   * 조회할 수 없는 노드(USER·DB·service group 등)를 거쳐 **같은 노드로 돌아왔을 때** 요청을 다시
   * 보내야 하는지 판단하는 데 쓴다. 대상 값이 그대로라 그것만으로는 구별할 수 없다.
   */
  const hadQueryableSelectionRef = React.useRef(true);
  const currentTargetData = useAtomValue(serverMapCurrentTargetDataAtom) as GetServerMap.NodeData;
  /**
   * service group(접힌 service) 노드는 조회 대상으로 삼지 않는다.
   *
   * group 노드에는 기준 application이 없는데도 `applicationName`에 serviceName이 들어 있어
   * (`flattenServiceMapResponse`), 그대로 보내면 service를 application인 것처럼 묻게 된다.
   * 팝업에서 자식 노드를 고르면 대상이 그 application으로 바뀌어 그때 조회가 시작된다.
   */
  const isGroupTarget = isServiceGroupTarget(currentTargetData);
  /**
   * 고른 것이 WAS가 아닌 상태(USER·DB·캐시·메시지 큐·UNKNOWN 노드, 그리고 링크).
   *
   * 액티브 스레드는 agent가 붙은 WAS만 가지므로 **차트 대신 안내 문구를 띄운다.** 요청은 그대로
   * 보낸다(예전 동작) — 화면 표시만 갈리는 판단이다.
   *
   * 핀과 무관하게 본다. 핀이 걸려 있을 때 이 판단을 건너뛰면, WAS가 아닌 노드를 고른 채 직전 WAS의
   * 액티브 스레드가 그려진다(USER 노드는 진입점 WAS와 이름이 같을 수 있어 특히 헷갈린다).
   *
   * map 응답이 아직 없어 노드 정보를 모를 때(`currentTargetData`가 undefined)는 판단을 보류한다.
   * 그러지 않으면 화면에 처음 들어왔을 때 안내 문구가 잠깐 스친다.
   */
  const isNotWasTarget =
    !!currentTargetData &&
    (currentTargetData as GetServerMap.NodeData).nodeCategory !== GetServerMap.NodeCategory.SERVER;
  // service group(접힌 service)만 조회 대상에서 뺀다. 그 외에는 고른 노드로 그대로 요청한다.
  const applicationName = isGroupTarget ? '' : currentServerMapTarget?.applicationName || '';
  const serviceType = isGroupTarget ? undefined : currentServerMapTarget?.serviceType;
  const screenServiceName = useServiceNameForLink();
  const { pathname } = useLocation();
  /**
   * 메시지에 service 정보를 실을 화면인지.
   *
   * **경로에 serviceName 세그먼트를 싣는 화면(servicemap 계열)에서만 싣는다.** 전역 선택값으로
   * 폴백하지 않는 것이 핵심이다 — `useServiceNameForLink`는 경로에 없으면 전역 선택값을 주므로,
   * 그것을 그대로 쓰면 servermap 실시간 보기에서도 실려 나가 예전과 다른 메시지가 된다. 그 화면은
   * 기존과 완전히 같은 메시지(`applicationName` 하나)를 보내야 한다.
   * (`useFilteredMapParameters`의 serviceName도 같은 이유로 폴백하지 않는다)
   *
   * `enableServiceMap`이 꺼져 있으면 `useServiceNameForLink`가 undefined를 반환하므로 servicemap
   * 경로에서도 빠진다 — service 개념이 없는 저장소로 새어 나가지 않게 하는 것은 그 훅 한 곳이다.
   */
  const carriesServiceInfo = !!screenServiceName && !!getServiceNameFromPath(pathname);
  // 고른 노드의 service가 화면의 service와 다를 수 있다(servicemap의 다른 service 노드).
  const requestServiceName = carriesServiceInfo ? (serviceName ?? screenServiceName) : undefined;
  const requestServiceType = carriesServiceInfo ? serviceType : undefined;
  // const { activeThreadCountsWithTotal, setActiveThreadCounts } = useActiveThread();
  const [activeThreadCounts, setActiveThreadCounts] = React.useState<AgentActiveThread.Response>();
  const [isApplicationLocked, setApplicationLock] = React.useState(true);
  const [showSetting, setShowSetting] = React.useState(false);
  const [setting, setSetting] = React.useState<AgentActiveSettingType>(DefaultValue);
  /**
   * 조회할 대상이 정해졌는지 여부.
   *
   * 대상이 없으면 activeThreadCount 요청을 보내지 않으므로 보여줄 응답도 없다. 그대로 두면 로딩
   * 스켈레톤이 영영 떠 있게 되므로, 로딩 대신 무엇을 해야 하는지 알려준다. (기준 application이
   * 없는 servicemap 실시간 보기에서 아직 노드를 고르지 않은 상태, WAS가 아닌 노드나 service
   * group을 고른 상태가 여기에 해당한다.)
   */
  const hasTarget = !!(applicationName || requestTarget.applicationName);

  React.useEffect(() => {
    connect();

    return () => {
      // 언마운트: 자동 재연결까지 막고 닫는다. 막지 않으면 close 이벤트가 다시 붙어서
      // 화면을 떠난 뒤에도 소켓이 살아남는다.
      disconnect();
    };
  }, []);

  /**
   * 고른 대상을 조회 대상으로 옮긴다.
   *
   * - **조회할 수 있는 대상(WAS)을 고르면 핀과 무관하게 그 대상으로 옮긴다.** 클릭한 노드의
   *   액티브 스레드를 보여주는 것이 이 패널의 목적이다. (핀이 걸려 있으면 클릭을 무시하던 예전
   *   동작에서 달라진 부분이다 — 다른 WAS를 눌러도 이전 노드의 응답만 계속 오는 상태가 된다)
   * - 조회할 수 없는 대상(WAS가 아닌 노드·service group·링크)을 골랐을 때는 **핀이 지금 보고 있는
   *   대상을 지켜 준다.** 핀이 풀려 있으면 대상을 비워 "WAS가 아니다"를 알린다.
   */
  React.useEffect(() => {
    const previousTarget = requestTargetRef.current;
    const hadQueryableSelection = hadQueryableSelectionRef.current;
    const shouldFollowTarget = !!applicationName || !isApplicationLocked;

    hadQueryableSelectionRef.current = !!applicationName;

    if (!shouldFollowTarget) {
      return;
    }
    if (
      previousTarget.applicationName === applicationName &&
      previousTarget.serviceType === requestServiceType &&
      previousTarget.serviceName === requestServiceName
    ) {
      if (!hadQueryableSelection) {
        // 조회할 수 없는 노드를 거쳐 같은 노드로 돌아왔다. 값이 그대로라 state는 두고 요청만 다시
        // 보낸다 — 그 사이 구독이 끊겼을 수 있고, 서버는 같은 이름이면 기존 구독을 유지한다.
        requestActiveThreadCount();
      }
      // 값도 그대로고 직전 선택도 그대로면 보낼 이유가 없다. (핀을 눌렀을 때가 여기에 해당한다.
      // 같은 요청을 또 보내면 서버가 'Already started' 에러를 남긴다)
      return;
    }
    // 세 값을 한 번에 갈아 끼운다. 따로 대입하면 중간 렌더에서 짝이 어긋난 요청이 나간다.
    setRequestTarget({
      applicationName,
      serviceType: requestServiceType,
      serviceName: requestServiceName,
    });
  }, [applicationName, requestServiceType, requestServiceName, isApplicationLocked]);

  /**
   * 조회 대상이 정해지면 그 대상으로 요청한다. (이미 열려 있을 때. 열리는 중이면 open 핸들러가 보낸다)
   *
   * **대상이 없어져도 소켓은 닫지 않는다.** 서버는 소켓 하나에 구독 하나이고 구독을 취소하는
   * 명령이 없어서(`activeThreadCount` 하나뿐이다), 닫는 것 말고는 직전 대상의 스트림을 멈출
   * 방법이 없다. 그래서 WAS가 아닌 노드를 골랐을 때는 직전 대상의 응답이 계속 오는데, 화면에
   * 쓰지 않고 흘려보낸다 — 연결은 화면이 열려 있는 동안 유지하는 것이 기존 동작이다.
   */
  React.useEffect(() => {
    requestActiveThreadCount();
  }, [requestTarget]);

  const connect = () => {
    shouldConnectRef.current = true;
    if (wsRef.current) {
      return; // 이미 열려 있거나 연결 중이다
    }
    initWebSocket();
  };

  const disconnect = () => {
    shouldConnectRef.current = false;
    wsRef.current?.close();
    wsRef.current = undefined;
    // 다음에 열릴 때를 위한 초기 상태로 되돌린다. CLOSED로 두면 다시 대상이 정해져 연결되는
    // 사이에 "연결이 종료되었습니다"가 스친다.
    setWebSocketState(WebSocket.CONNECTING);
  };

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
        // 대상이 정해진 뒤에 열기 때문에, 열리는 즉시 요청하면 첫 조회가 늦지 않는다.
        requestActiveThreadCount();
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
        eventController.abort();
        if (wsRef.current === ws) {
          wsRef.current = undefined;
        }
        if (!shouldConnectRef.current) {
          return; // 의도적으로 닫은 것이다
        }
        setWebSocketState(WebSocket.CLOSED);
        initWebSocket(); // 예기치 않게 끊긴 경우에만 다시 붙는다
      },
      { signal },
    );
  };

  const requestActiveThreadCount = () => {
    const target = requestTargetRef.current;

    if (!target.applicationName || wsRef.current?.readyState !== WebSocket.OPEN) {
      return;
    }
    sendMessage({ type: 'REQUEST', command: 'activeThreadCount', parameters: target });
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

  return (
    <div className="w-full h-full">
      {!hasTarget ? (
        <div className="flex justify-center items-center w-full h-full text-muted-foreground">
          {t('SERVER_MAP.REAL_TIME.SELECT_NODE')}
        </div>
      ) : webSocketState === WebSocket.OPEN ? (
        // 핀은 이 판단에 끼어들지 않는다. 고른 것이 WAS가 아니면 그 사실을 알려준다.
        !isNotWasTarget ? (
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
                {requestTarget.applicationName}
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
                applicationName={requestTarget.applicationName}
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
