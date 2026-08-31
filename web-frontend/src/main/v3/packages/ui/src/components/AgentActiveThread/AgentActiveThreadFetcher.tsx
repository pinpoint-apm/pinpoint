import React from 'react';
import { useTranslation } from 'react-i18next';
import { GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { AgentActiveThreadView } from './AgentActiveThreadView';
import { useLocation } from 'react-router';
import { useAtom, useAtomValue } from 'jotai';
import {
  activeThreadTargetAtom,
  serverMapCurrentTargetAtom,
  serverMapCurrentTargetDataAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { AgentActiveThreadSkeleton } from './AgentActiveThreadSkeleton';
import { useActiveThreadSocket } from './useActiveThreadSocket';
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

export const AgentActiveThreadFetcher = ({ serviceName }: AgentActiveThreadFetcherProps) => {
  const { t } = useTranslation();
  const [timezone] = useTimezone();
  const currentServerMapTarget = useAtomValue(serverMapCurrentTargetAtom);
  const { pathname } = useLocation();
  const [storedTarget, setStoredTarget] = useAtom(activeThreadTargetAtom);
  // 다른 화면에서 고정해 둔 대상은 이 화면의 것이 아니다. 경로가 같을 때만 이어 쓴다.
  const target = storedTarget?.path === pathname ? storedTarget : undefined;
  const { webSocketState, activeThreadCounts } = useActiveThreadSocket(target);
  const applicationName = currentServerMapTarget?.applicationName || '';
  const serviceType = currentServerMapTarget?.serviceType;
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
  const hasTarget = !!(applicationName || target?.applicationName);

  React.useEffect(() => {
    if (!applicationName) {
      return;
    }
    // 핀이 걸려 있으면 map에서 다른 노드를 골라도 조회 대상은 그대로다. 아직 대상이 없을 때
    // (이 화면에 처음 들어왔거나 다른 화면의 대상만 남아 있을 때)만 지금 고른 노드로 정한다.
    if (isApplicationLocked && target) {
      return;
    }
    // 값이 그대로면 새로 담지 않는다. 담으면 참조가 바뀌어 소켓 훅이 대상이 바뀐 것으로 보고,
    // 같은 application에 같은 요청을 한 번 더 보내게 된다(핀만 껐다 켠 경우).
    if (
      target?.applicationName === applicationName &&
      target?.serviceName === serviceName &&
      target?.serviceType === serviceType
    ) {
      return;
    }

    setStoredTarget({ path: pathname, applicationName, serviceName, serviceType });
  }, [
    pathname,
    applicationName,
    serviceName,
    serviceType,
    isApplicationLocked,
    target,
    setStoredTarget,
  ]);

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
                {target?.applicationName}
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
                applicationName={target?.applicationName}
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
