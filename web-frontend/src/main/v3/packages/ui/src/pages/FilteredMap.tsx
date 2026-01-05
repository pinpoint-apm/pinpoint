import React from 'react';
import { useAtom } from 'jotai';
import { Link, useNavigate } from 'react-router-dom';
import {
  convertParamsToQueryString,
  getServerImagePath,
  getFilteredMapPath,
  getApplicationKey,
  getServerMapPath,
} from '@pinpoint-fe/ui/src/utils';
import { useFilteredMapParameters } from '@pinpoint-fe/ui/src/hooks';
import {
  serverMapDataAtom,
  serverMapCurrentTargetAtom,
  scatterDataByApplicationKeyAtom,
  CurrentTarget,
} from '@pinpoint-fe/ui/src/atoms';
import {
  FilteredMapType as FilteredMap,
  GetServerMap,
  Configuration,
} from '@pinpoint-fe/ui/src/constants';
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
  configuration?: Configuration & Record<string, string>;
  ApplicationList?: (props: ApplicationCombinedListProps) => React.ReactElement;
}

const FILTERED_MAP_CONTAINER_ID = 'filtered-map-main-container';

export const FilteredMapPage = ({
  authorizationGuideUrl,
  configuration,
  ApplicationList = ApplicationCombinedList,
}: FilteredMapPageProps) => {
  const periodMax = configuration?.[`periodMax.serverMap`];
  const periodInterval = configuration?.['periodInterval.serverMap'];
  const containerRef = React.useRef<HTMLDivElement>(null);
  const navigate = useNavigate();
  const { dateRange, application, parsedFilters, parsedHint, searchParameters, search } =
    useFilteredMapParameters();
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
            agents: (serverMapData?.applicationMapData.nodeDataArray as FilteredMap.NodeData[])
              ?.find((n) => n.key === `${prevFilter.applicationName}^${prevFilter.serviceType}`)
              ?.agents?.map((agent) => agent.id),
          };
        } else if (
          prevFilter.fromApplication &&
          prevFilter.fromServiceType &&
          prevFilter.toApplication &&
          prevFilter.toServiceType
        ) {
          const linkData = (
            serverMapData?.applicationMapData.linkDataArray as FilteredMap.LinkData[]
          )?.find(
            (n) =>
              n.key ===
              `${prevFilter.fromApplication}^${prevFilter.fromServiceType}~${prevFilter.toApplication}^${prevFilter.toServiceType}`,
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
          ({ key }) => key === serverMapCurrentTarget.id,
        ) ||
          (serverMapData.applicationMapData.linkDataArray as GetServerMap.LinkData[]).some(
            ({ key }) => key === serverMapCurrentTarget.id,
          ));

      if (isTargetIncluded || serverMapCurrentTarget?.nodes || serverMapCurrentTarget?.edges) {
        currentTarget = serverMapCurrentTarget;
        setServerMapCurrentTarget(currentTarget);
      } else {
        const applicationInfo = (
          serverMapData.applicationMapData.nodeDataArray as GetServerMap.NodeData[]
        ).find((node) => {
          return (
            getApplicationKey(application!) === node.key ||
            (node.applicationName === application?.applicationName &&
              node.serviceType === 'UNAUTHORIZED')
          );
        })!;
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
        navigate(
          `${getFilteredMapPath(
            parsedFilters[parsedFilters.length - 1],
          )}?${convertParamsToQueryString(formattedDates)}&${convertParamsToQueryString({
            filter: searchParameters.filter,
            hint: searchParameters.hint,
          })}`,
        );
      }
    }) as DatetimePickerChangeHandler,
    [application?.applicationName, searchParameters.filter, searchParameters.hint],
  );

  // FilterWizard
  const handleClickApply = useFilterWizardOnClickApply<FilteredMap.LinkData>({
    from: searchParameters.from,
    to: searchParameters.to,
    parsedHint,
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
  });

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        title={
          <div className="flex items-center gap-2">
            <PiTreeStructureDuotone />
            <Link className="hover:underline" to={getServerMapPath(application)}>
              Servermap
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
                configuration={configuration}
              />
            )}
          </LayoutWithHorizontalResizable>
        </div>
      )}
    </div>
  );
};
