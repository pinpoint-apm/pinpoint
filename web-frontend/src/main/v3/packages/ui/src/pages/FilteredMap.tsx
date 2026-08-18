import React from 'react';
import { useAtom } from 'jotai';
import { Link, useNavigate } from 'react-router-dom';
import {
  convertParamsToQueryString,
  getServerImagePath,
  getFilteredMapPathOfApplication,
  getServerMapPath,
  getServiceMapPath,
  findNodeOfApplication,
  findLinkOfApplications,
} from '@pinpoint-fe/ui/src/utils';
import { useConfiguration, useFilteredMapParameters } from '@pinpoint-fe/ui/src/hooks';
import {
  serverMapDataAtom,
  serverMapCurrentTargetAtom,
  scatterDataByApplicationKeyAtom,
  CurrentTarget,
} from '@pinpoint-fe/ui/src/atoms';
import { FilteredMapType as FilteredMap, GetServerMap } from '@pinpoint-fe/ui/src/constants';
import {
  ApplicationCombinedList,
  ApplicationCombinedListProps,
  FilteredMap as FilteredMapComponent,
  LayoutWithHorizontalResizable,
} from '@pinpoint-fe/ui';
import { differenceInMinutes } from 'date-fns';
import { useUpdateEffect } from 'usehooks-ts';
import { useTranslation } from 'react-i18next';
import {
  DatetimePicker,
  DatetimePickerChangeHandler,
  FilterWizard,
  MainHeader,
  ProgressBarWithControls,
} from '@pinpoint-fe/ui';
import { PiTreeStructureDuotone } from 'react-icons/pi';
import {
  useFilterWizardOnClickApply,
  useServerMapOnClickMenuItem,
} from '@pinpoint-fe/ui/src/hooks/serverMap';
import { FilteredMapChartsBoard } from '@pinpoint-fe/ui/src/components/FilterMap/FilteredMapChartsBoard';

export interface FilteredMapPageProps {
  authorizationGuideUrl?: string;
  ApplicationList?: (props: ApplicationCombinedListProps) => React.ReactElement;
}

const FILTERED_MAP_CONTAINER_ID = 'filtered-map-main-container';

export const FilteredMapPage = ({
  authorizationGuideUrl,
  ApplicationList = ApplicationCombinedList,
}: FilteredMapPageProps) => {
  const configuration = useConfiguration();
  const periodMax = configuration?.[`periodMax.serverMap`];
  const periodInterval = configuration?.['periodInterval.serverMap'];
  const containerRef = React.useRef<HTMLDivElement>(null);
  const navigate = useNavigate();
  const {
    dateRange,
    application,
    serviceName,
    parsedFilters,
    parsedHint,
    searchParameters,
    search,
  } = useFilteredMapParameters();
  // 경로에 serviceName이 실려 있으면 servicemap에서 넘어온 화면이다. 돌아갈 map도 그쪽이어야 한다.
  // (servermap에서 넘어온 화면은 예전과 같이 servermap으로 돌아간다.)
  const parentMap = serviceName
    ? { title: 'Servicemap', path: getServiceMapPath(serviceName, application) }
    : { title: 'Servermap', path: getServerMapPath(application) };
  const [serverMapCurrentTarget, setServerMapCurrentTarget] = useAtom(serverMapCurrentTargetAtom);
  const [serverMapData, setServerMapData] = useAtom(serverMapDataAtom);
  const [appliedFilters, setAppliedFilters] =
    React.useState<FilteredMap.FilterState[]>(parsedFilters);
  const [filter, setFilter] = React.useState<FilteredMap.FilterState>();
  const [showFilterConfig, setShowFilterConfig] = React.useState(false);
  const [scatterDataByApplicationKey, setScatterDataByApplicationKey] = useAtom(
    scatterDataByApplicationKeyAtom,
  );
  const [pauseFilteredMapFetcher, setPauseFilteredMapFetcher] = React.useState(false);
  const { t } = useTranslation();

  useUpdateEffect(() => {
    setServerMapData(undefined);
    setScatterDataByApplicationKey(undefined);
    setPauseFilteredMapFetcher(false);
  }, [search]);

  React.useEffect(() => {
    setAppliedFilters((prev) => {
      return prev.map((prevFilter) => {
        if (prevFilter.applicationName && prevFilter.serviceType) {
          return {
            ...prevFilter,
            agents: findNodeOfApplication(
              serverMapData?.applicationMapData.nodeDataArray as FilteredMap.NodeData[],
              { applicationName: prevFilter.applicationName, serviceType: prevFilter.serviceType },
            )?.agents?.map((agent) => agent.id),
          };
        } else if (
          prevFilter.fromApplication &&
          prevFilter.fromServiceType &&
          prevFilter.toApplication &&
          prevFilter.toServiceType
        ) {
          const linkData = findLinkOfApplications(
            serverMapData?.applicationMapData.linkDataArray as FilteredMap.LinkData[],
            {
              applicationName: prevFilter.fromApplication,
              serviceType: prevFilter.fromServiceType,
            },
            { applicationName: prevFilter.toApplication, serviceType: prevFilter.toServiceType },
          );

          return {
            ...prevFilter,
            fromAgents: linkData?.fromAgents?.map((agent) => agent.id),
            toAgents: linkData?.toAgents?.map((agent) => agent.id),
          };
        }
        return prevFilter;
      });
    });

    if (
      serverMapData &&
      serverMapData?.applicationMapData?.nodeDataArray &&
      serverMapData?.applicationMapData?.nodeDataArray.length
    ) {
      let currentTarget: CurrentTarget;
      const isTargetIncluded =
        serverMapCurrentTarget &&
        ((serverMapData.applicationMapData.nodeDataArray as GetServerMap.NodeData[]).some(
          ({ key, nodeKey }) =>
            key === serverMapCurrentTarget.id || nodeKey === serverMapCurrentTarget.id,
        ) ||
          (serverMapData.applicationMapData.linkDataArray as GetServerMap.LinkData[]).some(
            ({ key, linkKey }) =>
              key === serverMapCurrentTarget.id || linkKey === serverMapCurrentTarget.id,
          ));

      if (isTargetIncluded || serverMapCurrentTarget?.nodes || serverMapCurrentTarget?.edges) {
        currentTarget = serverMapCurrentTarget;
        setServerMapCurrentTarget(currentTarget);
      } else {
        // 경로의 application에 해당하는 노드를 골라 기준(base node)으로 세운다. key 형식이
        // 설정에 따라 2단/3단으로 갈리므로(`NodeRender.detailedRender`) 형식에 무관한 매처를 쓴다.
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

  React.useEffect(() => {
    return () => {
      setScatterDataByApplicationKey(undefined);
    };
  }, []);

  const handleChangeDateRagePicker = React.useCallback(
    (({ formattedDates }) => {
      if (formattedDates) {
        // 기간만 바꾼다. 경로의 application은 지금 것을 그대로 유지한다 — 필터에서 다시 뽑으면
        // 링크 필터의 기준이 sourceIsWas 없이 도착지로 고정돼, 출발지가 기준이던 경로가 뒤집힌다.
        navigate(
          `${getFilteredMapPathOfApplication(
            application,
            serviceName,
          )}?${convertParamsToQueryString(formattedDates)}&${convertParamsToQueryString({
            filter: searchParameters.filter,
            hint: searchParameters.hint,
          })}`,
        );
      }
    }) as DatetimePickerChangeHandler,
    [
      application?.applicationName,
      application?.serviceType,
      searchParameters.filter,
      searchParameters.hint,
      serviceName,
    ],
  );

  // FilterWizard
  // 여기서 필터를 더 걸면 또 새 탭이 열린다. 지금 보고 있는 service를 그대로 이어야 한다.
  const handleClickApply = useFilterWizardOnClickApply<FilteredMap.LinkData>({
    from: searchParameters.from,
    to: searchParameters.to,
    parsedHint,
    serviceName,
  });

  // ServerMapCore
  const handleClickMenuItem = useServerMapOnClickMenuItem<
    FilteredMap.NodeData,
    FilteredMap.LinkData
  >({
    from: searchParameters.from,
    to: searchParameters.to,
    parsedHint,
    parsedFilters,
    setFilter,
    setShowFilterConfig,
    serviceName,
  });

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        title={
          <div className="flex items-center gap-2">
            <PiTreeStructureDuotone />
            <Link className="hover:underline" to={parentMap.path}>
              {parentMap.title}
            </Link>{' '}
            / Filtered
          </div>
        }
      >
        <ApplicationList selectedApplication={application} disabled />
        <div className="ml-auto">
          {application && (
            <DatetimePicker
              from={searchParameters.from}
              to={searchParameters.to}
              onChange={handleChangeDateRagePicker}
              maxDateRangeDays={periodMax}
              outOfDateRangeMessage={t('DATE_RANGE_PICKER.MAX_SEARCH_PERIOD', {
                maxSearchPeriod: periodMax,
              })}
              timeUnits={periodInterval}
            />
          )}
        </div>
      </MainHeader>
      {application && (
        <div
          id={FILTERED_MAP_CONTAINER_ID}
          className="relative flex-1 h-full overflow-x-hidden"
          ref={containerRef}
        >
          <LayoutWithHorizontalResizable>
            <div className="relative flex flex-col w-full h-full gap-4">
              {application && (
                <>
                  <div className="absolute top-3 left-3 z-[1] bg-background rounded-lg shadow-lg border">
                    <FilterWizard
                      appliedFilters={appliedFilters}
                      tempFilter={filter}
                      openConfigures={showFilterConfig}
                      onClickShowConfig={() => setShowFilterConfig(!showFilterConfig)}
                      onClickApply={handleClickApply}
                    />
                  </div>
                  <ProgressBarWithControls
                    className="relative z-[1] top-3 left-90 w-[calc(100%-23.5rem)] shadow border"
                    progress={(serverMapData as FilteredMap.Response)?.lastFetchedTimestamp}
                    range={[dateRange.to.getTime(), dateRange.from.getTime()]}
                    tickCount={
                      differenceInMinutes(dateRange.to, dateRange.from) < 5
                        ? differenceInMinutes(dateRange.to, dateRange.from)
                        : 5
                    }
                    onClickPause={() => setPauseFilteredMapFetcher(true)}
                    onClickResume={() => setPauseFilteredMapFetcher(false)}
                  />
                  <FilteredMapComponent
                    isPaused={pauseFilteredMapFetcher}
                    onClickMenuItem={handleClickMenuItem}
                  />
                </>
              )}
            </div>
            {({ currentPanelWidth, SERVER_LIST_WIDTH, resizeHandleWidth }) => (
              <FilteredMapChartsBoard
                authorizationGuideUrl={authorizationGuideUrl}
                currentPanelWidth={currentPanelWidth}
                SERVER_LIST_WIDTH={SERVER_LIST_WIDTH}
                resizeHandleWidth={resizeHandleWidth}
                FILTERED_MAP_CONTAINER_ID={FILTERED_MAP_CONTAINER_ID}
              />
            )}
          </LayoutWithHorizontalResizable>
        </div>
      )}
    </div>
  );
};
