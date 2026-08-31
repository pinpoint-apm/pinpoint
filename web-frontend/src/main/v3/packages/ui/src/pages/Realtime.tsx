import React from 'react';
import { useAtom } from 'jotai';
import { useNavigate } from 'react-router';
import { getServerMapPath, getServerImagePath, getRealtimePath } from '@pinpoint-fe/ui/src/utils';
import { serverMapCurrentTargetAtom } from '@pinpoint-fe/ui/src/atoms';
import { useServerMapSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import {
  ApplicationCombinedList,
  ApplicationCombinedListProps,
  DatetimePicker,
  DatetimePickerChangeHandler,
  HelpPopover,
  MainHeader,
  Realtime,
  ServerMap,
} from '@pinpoint-fe/ui';
import { PiTreeStructureDuotone } from 'react-icons/pi';

export interface RealtimePageProps {
  ApplicationList?: (props: ApplicationCombinedListProps) => React.ReactElement;
  /**
   * map 영역에 그릴 컴포넌트(기본값 ServerMap).
   * servicemap 실시간 보기는 ServiceMap을 넘긴다. 화면은 그대로 두고 map을 그리는 API만 갈린다.
   */
  MapView?: typeof ServerMap;
  title?: 'Servermap' | 'Servicemap';
  /** 실시간 보기를 벗어날 때(기간 선택, application 선택) 쓸 경로 생성 함수 */
  getPagePath?: typeof getServerMapPath;
  /** 실시간 보기 안에서 이동할 때 쓸 경로 생성 함수 */
  getRealtimePagePath?: typeof getRealtimePath;
  /**
   * map이 기준 application을 필요로 하는지 여부(기본값 true).
   *
   * false면 application을 고르지 않아도 map을 그리므로 선택 박스 대신 service 이름을 보여준다.
   * servicemap에서 DEFAULT가 아닌 service를 볼 때가 여기에 해당한다.
   * (그 service에 소속된 application을 모두 모아 그리므로 고를 대상이 없다.)
   */
  requiresApplication?: boolean;
  /** 헤더에 표시할 service 이름. application 선택 박스를 대신한다. */
  serviceName?: string;
}

export const RealtimePage = ({
  ApplicationList = ApplicationCombinedList,
  MapView = ServerMap,
  title = 'Servermap',
  getPagePath = getServerMapPath,
  getRealtimePagePath = getRealtimePath,
  requiresApplication = true,
  serviceName,
}: RealtimePageProps) => {
  const navigate = useNavigate();
  const { application, searchParameters } = useServerMapSearchParameters();
  const [serverMapCurrentTarget, setServerMapCurrentTarget] = useAtom(serverMapCurrentTargetAtom);
  // 기준 application이 필요 없는 map은 고를 대상이 없으므로 곧바로 그린다.
  const showMap = !requiresApplication || !!application;
  // const [setCurrentServer] = useAtom(curr entServerAtom);
  // const sizes = useAtomValue(chartsBoardSizesAtom);

  React.useEffect(() => {
    if (application) {
      setServerMapCurrentTarget({
        ...application,
        imgPath: getServerImagePath(application),
        type: 'node',
      });
    } else {
      setServerMapCurrentTarget(undefined);
    }
  }, [application?.applicationName, application?.serviceType]);

  // const handleClickNode = ({ label, type, imgPath }: MergedNode) => {
  //   setServerMapCurrentTarget({
  //     applicationName: label,
  //     serviceType: type,
  //     imgPath: imgPath!,
  //   });
  //   setCurrentServer(undefined);
  //   setScatterData(undefined);
  // };

  const handleChangeDateRagePicker = React.useCallback(
    (({ isRealtime }) => {
      if (isRealtime) {
        navigate(`${getRealtimePagePath(application!)}`);
      } else {
        navigate(`${getPagePath(application!)}`);
      }
    }) as DatetimePickerChangeHandler,
    [application, getPagePath, getRealtimePagePath],
  );

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        title={
          <div className="flex items-center gap-2">
            <PiTreeStructureDuotone />
            <div className="flex items-center gap-1">
              {title}
              <HelpPopover helpKey="HELP_VIEWER.SERVER_MAP" />
            </div>
          </div>
        }
      >
        {requiresApplication ? (
          <ApplicationList
            selectedApplication={application}
            onClickApplication={(application) => navigate(getPagePath(application))}
          />
        ) : (
          <div className="text-sm font-medium truncate">{serviceName}</div>
        )}
        <div className="ml-auto">
          {showMap && (
            <DatetimePicker
              isRealtime
              enableRealtimeButton
              from={searchParameters.from}
              to={searchParameters.to}
              onChange={handleChangeDateRagePicker}
            />
          )}
        </div>
      </MainHeader>
      {/* 기준 application이 있는 map은 그 application이 곧 조회 대상이라 정해지기를 기다린다.
          기준이 없는 map은 노드를 고르기 전에도 map 자체는 그려야 고를 수 있다. */}
      {(!requiresApplication || serverMapCurrentTarget) && (
        <Realtime MapView={MapView} requiresApplication={requiresApplication} />
      )}
    </div>
  );
};
