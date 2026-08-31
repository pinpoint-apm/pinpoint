import React from 'react';
import { useAtom, useAtomValue } from 'jotai';
import { useNavigate } from 'react-router';
import { useTranslation } from 'react-i18next';
import {
  getServerMapPath,
  convertParamsToQueryString,
  getServerImagePath,
  findNodeOfApplication,
  getFormattedDateRange,
  getRealtimePath,
} from '@pinpoint-fe/ui/src/utils';
import { useConfiguration, useServerMapSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import {
  serverMapDataAtom,
  serverMapCurrentTargetAtom,
  CurrentTarget,
} from '@pinpoint-fe/ui/src/atoms';
import { FilteredMapType as FilteredMap, GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { IoMdClose } from 'react-icons/io';
import {
  DatetimePicker,
  DatetimePickerChangeHandler,
  FilterWizard,
  MainHeader,
  ServerMap,
  LayoutWithHorizontalResizable,
  ApplicationCombinedList,
  HelpPopover,
  ApplicationCombinedListProps,
} from '@pinpoint-fe/ui';
import { PiTreeStructureDuotone } from 'react-icons/pi';
import {
  useFilterWizardOnClickApply,
  useServerMapOnClickMenuItem,
} from '@pinpoint-fe/ui/src/hooks/serverMap';
import { ServerMapChartsBoard } from '@pinpoint-fe/ui/src/components/ServerMap/ServerMapChartBoard';

export interface ServermapPageProps {
  authorizationGuideUrl?: string;
  ApplicationList?: (props: ApplicationCombinedListProps) => React.ReactElement;
  MapView?: typeof ServerMap;
  title?: 'Servermap' | 'Servicemap';
  /** 페이지 내부 이동(애플리케이션 선택, 기간 변경 등)에 사용할 경로 생성 함수 */
  getPagePath?: typeof getServerMapPath;
  /** 실시간 보기로 이동할 때 사용할 경로 생성 함수 */
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

const SERVERMAP_CONTAINER_ID = 'server-map-main-container';

export const ServerMapPage = ({
  authorizationGuideUrl,
  ApplicationList = ApplicationCombinedList,
  MapView = ServerMap,
  title = 'Servermap',
  getPagePath = getServerMapPath,
  getRealtimePagePath = getRealtimePath,
  requiresApplication = true,
  serviceName,
}: ServermapPageProps) => {
  const configuration = useConfiguration();
  const periodMax = configuration?.[`periodMax.serverMap`];
  const periodInterval = configuration?.[`periodInterval.serverMap`];
  const containerRef = React.useRef<HTMLDivElement>(null);
  const navigate = useNavigate();
  const { dateRange, application, search, searchParameters, queryOption, pathname } =
    useServerMapSearchParameters();
  const [serverMapCurrentTarget, setServerMapCurrentTarget] = useAtom(serverMapCurrentTargetAtom);
  const serverMapData = useAtomValue(serverMapDataAtom);
  const [showFilter, setShowFilter] = React.useState(false);
  const [filter, setFilter] = React.useState<FilteredMap.FilterState>();
  const { t } = useTranslation();
  // 기준 application이 필요 없는 map은 고를 대상이 없으므로 곧바로 그린다.
  const showMap = !requiresApplication || !!application;

  // 기준 application이 필요 없는 map에서는 경로의 application 세그먼트가 아무 의미를 갖지 않는다.
  // 다른 화면에서 고른 application이 링크를 타고 실려 들어올 수 있는데, 그대로 두면 노드를
  // 클릭했을 때 통계 조회의 기준 application이 그 값으로 잡혀 클릭한 노드와 다른 데이터를
  // 보여준다. 그래서 경로에서 지운다. (기간은 유지해야 하므로 query string은 그대로 넘긴다.)
  React.useEffect(() => {
    if (!requiresApplication && application) {
      navigate(`${getPagePath(null)}${search}`, { replace: true });
    }
    // getPagePath는 servicemap에서 serviceName을 캡처하므로(useCallback) service가 바뀌면 바뀐다.
    // 빠뜨리면 이전 service의 경로로 보낼 수 있다.
  }, [
    requiresApplication,
    application?.applicationName,
    application?.serviceType,
    search,
    getPagePath,
    navigate,
  ]);

  React.useEffect(() => {
    initPage();
  }, [pathname]);

  React.useEffect(() => {
    setShowFilter(false);

    if (
      serverMapData &&
      serverMapData?.applicationMapData?.nodeDataArray &&
      serverMapData?.applicationMapData?.nodeDataArray.length
    ) {
      let currentTarget: CurrentTarget;
      const isTargetIncluded =
        serverMapCurrentTarget &&
        ((serverMapData.applicationMapData.nodeDataArray as GetServerMap.NodeData[]).some(
          (node) =>
            node.key === serverMapCurrentTarget.id || node.nodeKey === serverMapCurrentTarget.id,
        ) ||
          (serverMapData.applicationMapData.linkDataArray as GetServerMap.LinkData[]).some(
            (link) =>
              link.key === serverMapCurrentTarget.id || link.linkKey === serverMapCurrentTarget.id,
          ));

      if (isTargetIncluded || serverMapCurrentTarget?.nodes || serverMapCurrentTarget?.edges) {
        currentTarget = serverMapCurrentTarget;
        setServerMapCurrentTarget(currentTarget);
      } else {
        // key 형식이 map API마다 다르므로(servermap 2단, servicemap 3단) 형식에 무관한 매처를 쓴다.
        const applicationInfo = findNodeOfApplication(
          serverMapData.applicationMapData.nodeDataArray as GetServerMap.NodeData[],
          application,
        );

        if (applicationInfo) {
          const { applicationName, serviceType } = applicationInfo;
          currentTarget = {
            applicationName,
            serviceType,
            imgPath: getServerImagePath({ applicationName, serviceType }),
            type: 'node',
          };
          setServerMapCurrentTarget(currentTarget);
        }
      }
    } else {
      setServerMapCurrentTarget(undefined);
    }
  }, [serverMapData]);

  const handleChangeDateRagePicker = React.useCallback(
    (({ formattedDates: formattedDate, isRealtime }) => {
      if (isRealtime) {
        navigate(`${getRealtimePagePath(application!)}`);
      } else {
        navigate(
          `${getPagePath(application!)}?${convertParamsToQueryString({
            ...formattedDate,
            ...queryOption,
          })}`,
        );
      }
    }) as DatetimePickerChangeHandler,
    [application?.applicationName, queryOption, getPagePath, getRealtimePagePath],
  );

  const initPage = () => {
    setServerMapCurrentTarget(undefined);
    setShowFilter(false);
  };

  // FilterWizard
  // filteredMap은 새 탭으로 열리므로, servicemap에서 열었다면 그 service를 경로에 실어야 한다.
  // (servermap에서는 serviceName prop이 없어 예전과 같은 경로가 된다.)
  const handleClickApply = useFilterWizardOnClickApply<GetServerMap.LinkData>({
    from: searchParameters.from,
    to: searchParameters.to,
    serviceName,
  });

  const handleClickMenuItem = useServerMapOnClickMenuItem<
    GetServerMap.NodeData,
    GetServerMap.LinkData
  >({
    from: searchParameters.from,
    to: searchParameters.to,
    setFilter,
    setShowFilter,
    serviceName,
  });

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
            open={!application}
            selectedApplication={application}
            onClickApplication={(application) => navigate(getPagePath(application))}
          />
        ) : (
          <div className="text-sm font-medium truncate">{serviceName}</div>
        )}
        {showMap && (
          <div className="flex gap-1 ml-auto">
            <DatetimePicker
              enableRealtimeButton
              from={searchParameters.from}
              to={searchParameters.to}
              onChange={handleChangeDateRagePicker}
              maxDateRangeDays={periodMax}
              outOfDateRangeMessage={t('DATE_RANGE_PICKER.MAX_SEARCH_PERIOD', {
                maxSearchPeriod: periodMax,
              })}
              timeUnits={periodInterval}
            />
            <HelpPopover helpKey="HELP_VIEWER.NAVBAR" />
          </div>
        )}
      </MainHeader>
      {showMap && (
        <div
          id={SERVERMAP_CONTAINER_ID}
          className="relative flex-1 h-full overflow-x-hidden"
          ref={containerRef}
        >
          <LayoutWithHorizontalResizable disabled={!serverMapCurrentTarget}>
            <div className="relative w-full h-full">
              {showFilter && (
                <div className="absolute top-3 left-3 z-[1] bg-background rounded-lg shadow-lg border">
                  <button
                    className="absolute text-xl top-3 right-3 text-muted-foreground"
                    onClick={() => setShowFilter(false)}
                  >
                    <IoMdClose />
                  </button>
                  <FilterWizard
                    hideStatus={true}
                    tempFilter={filter}
                    openConfigures={true}
                    onClickApply={handleClickApply}
                  />
                </div>
              )}
              <MapView
                queryOption={queryOption}
                onApplyChangedOption={(option) => {
                  navigate(
                    `${getPagePath(application)}?${convertParamsToQueryString({
                      ...getFormattedDateRange(dateRange),
                      ...option,
                    })}`,
                  );
                }}
                onClickMenuItem={handleClickMenuItem}
              />
            </div>
            {({ currentPanelWidth, SERVER_LIST_WIDTH, resizeHandleWidth }) => (
              <ServerMapChartsBoard
                authorizationGuideUrl={authorizationGuideUrl}
                currentPanelWidth={currentPanelWidth}
                SERVER_LIST_WIDTH={SERVER_LIST_WIDTH}
                resizeHandleWidth={resizeHandleWidth}
                SERVERMAP_CONTAINER_ID={SERVERMAP_CONTAINER_ID}
              />
            )}
          </LayoutWithHorizontalResizable>
        </div>
      )}
    </div>
  );
};
