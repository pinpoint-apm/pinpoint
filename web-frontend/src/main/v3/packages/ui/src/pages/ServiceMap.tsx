import React from 'react';
import { useAtom, useAtomValue } from 'jotai';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  getServiceMapPath,
  getRealtimePath,
  convertParamsToQueryString,
  getServerImagePath,
  getFormattedDateRange,
} from '@pinpoint-fe/ui/src/utils';
import { useServerMapSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import {
  serverMapDataAtom,
  serverMapCurrentTargetAtom,
  CurrentTarget,
} from '@pinpoint-fe/ui/src/atoms';
import {
  FilteredMapType as FilteredMap,
  GetServerMap,
  GetServiceMap,
  Configuration,
} from '@pinpoint-fe/ui/src/constants';
import { IoMdClose } from 'react-icons/io';
import {
  DatetimePicker,
  DatetimePickerChangeHandler,
  FilterWizard,
  MainHeader,
  LayoutWithHorizontalResizable,
  ApplicationCombinedList,
  HelpPopover,
  ApplicationCombinedListProps,
  ErrorBoundary,
} from '@pinpoint-fe/ui';
import { PiTreeStructureDuotone } from 'react-icons/pi';
import {
  useFilterWizardOnClickApply,
  useServerMapOnClickMenuItem,
} from '@pinpoint-fe/ui/src/hooks/serverMap';
import { ServerMapChartsBoard } from '@pinpoint-fe/ui/src/components/ServerMap/ServerMapChartBoard';
import { ServerMapSkeleton } from '@pinpoint-fe/ui/src/components/ServerMap/ServerMapSkeleton';
import { ServiceMapFetcher } from '@pinpoint-fe/ui/src/components/ServerMap/ServiceMapFetcher';

export interface ServiceMapPageProps {
  authorizationGuideUrl?: string;
  configuration?: Configuration & Record<string, string>;
  ApplicationList?: (props: ApplicationCombinedListProps) => React.ReactElement;
}

const SERVICEMAP_CONTAINER_ID = 'service-map-main-container';

export const ServiceMapPage = ({
  authorizationGuideUrl,
  configuration,
  ApplicationList = ApplicationCombinedList,
}: ServiceMapPageProps) => {
  const periodMax = configuration?.[`periodMax.serverMap`];
  const periodInterval = configuration?.[`periodInterval.serverMap`];
  const containerRef = React.useRef<HTMLDivElement>(null);
  const navigate = useNavigate();
  const { dateRange, application, searchParameters, queryOption, pathname } =
    useServerMapSearchParameters();
  const [serverMapCurrentTarget, setServerMapCurrentTarget] = useAtom(serverMapCurrentTargetAtom);
  const serverMapData = useAtomValue(serverMapDataAtom);
  const [showFilter, setShowFilter] = React.useState(false);
  const [filter, setFilter] = React.useState<FilteredMap.FilterState>();
  const { t } = useTranslation();

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
      const nodeDataArray = serverMapData.applicationMapData.nodeDataArray as (
        | GetServiceMap.NodeData
        | GetServiceMap.ServiceGroupNodeData
      )[];
      const linkDataArray = serverMapData.applicationMapData.linkDataArray as GetServerMap.LinkData[];

      const isTargetIncluded =
        serverMapCurrentTarget &&
        (nodeDataArray.some(({ key }) => key === serverMapCurrentTarget.id) ||
          linkDataArray.some(({ key }) => key === serverMapCurrentTarget.id));

      if (isTargetIncluded || serverMapCurrentTarget?.nodes || serverMapCurrentTarget?.edges) {
        currentTarget = serverMapCurrentTarget;
        setServerMapCurrentTarget(currentTarget);
      } else {
        // ServiceMap node keys use "serviceName^applicationName^serviceType" format,
        // so find the base node by matching applicationName and serviceType fields.
        const applicationInfo = nodeDataArray.find(
          (node): node is GetServiceMap.NodeData =>
            !GetServiceMap.isServiceGroupNode(node) &&
            (node.applicationName === application?.applicationName &&
              (node.serviceType === application?.serviceType ||
                node.serviceType === 'UNAUTHORIZED')),
        );

        if (applicationInfo) {
          const { key, applicationName, serviceType } = applicationInfo;
          currentTarget = {
            id: key,
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

  const handleChangeDateRangePicker = React.useCallback(
    (({ formattedDates: formattedDate, isRealtime }) => {
      if (isRealtime) {
        navigate(`${getRealtimePath(application!)}`);
      } else {
        navigate(
          `${getServiceMapPath(application!)}?${convertParamsToQueryString({
            ...formattedDate,
            ...queryOption,
          })}`,
        );
      }
    }) as DatetimePickerChangeHandler,
    [application?.applicationName, queryOption],
  );

  const initPage = () => {
    setServerMapCurrentTarget(undefined);
    setShowFilter(false);
  };

  const handleClickApply = useFilterWizardOnClickApply<GetServerMap.LinkData>({
    from: searchParameters.from,
    to: searchParameters.to,
  });

  const handleClickMenuItem = useServerMapOnClickMenuItem<
    GetServerMap.NodeData,
    GetServerMap.LinkData
  >({
    from: searchParameters.from,
    to: searchParameters.to,
    setFilter,
    setShowFilter,
  });

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        title={
          <div className="flex items-center gap-2">
            <PiTreeStructureDuotone />
            <div className="flex items-center gap-1">
              Servicemap
              <HelpPopover helpKey="HELP_VIEWER.SERVER_MAP" />
            </div>
          </div>
        }
      >
        <ApplicationList
          open={!application}
          selectedApplication={application}
          onClickApplication={(application) => navigate(getServiceMapPath(application))}
        />
        {application && (
          <div className="flex gap-1 ml-auto">
            <DatetimePicker
              enableRealtimeButton
              from={searchParameters.from}
              to={searchParameters.to}
              onChange={handleChangeDateRangePicker}
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
      {application && (
        <div
          id={SERVICEMAP_CONTAINER_ID}
          className="relative flex-1 h-full overflow-x-hidden"
          ref={containerRef}
        >
          <LayoutWithHorizontalResizable disabled={!serverMapCurrentTarget}>
            <div className="relative w-full h-full">
              {application && (
                <>
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
                  <ErrorBoundary>
                    <React.Suspense fallback={<ServerMapSkeleton className="w-full h-full" />}>
                      <ServiceMapFetcher
                        queryOption={queryOption}
                        onApplyChangedOption={(option) => {
                          navigate(
                            `${getServiceMapPath(application)}?${convertParamsToQueryString({
                              ...getFormattedDateRange(dateRange),
                              ...option,
                            })}`,
                          );
                        }}
                        onClickMenuItem={handleClickMenuItem}
                      />
                    </React.Suspense>
                  </ErrorBoundary>
                </>
              )}
            </div>
            {({ currentPanelWidth, SERVER_LIST_WIDTH, resizeHandleWidth }) => (
              <ServerMapChartsBoard
                authorizationGuideUrl={authorizationGuideUrl}
                currentPanelWidth={currentPanelWidth}
                SERVER_LIST_WIDTH={SERVER_LIST_WIDTH}
                resizeHandleWidth={resizeHandleWidth}
                SERVERMAP_CONTAINER_ID={SERVICEMAP_CONTAINER_ID}
                configuration={configuration}
              />
            )}
          </LayoutWithHorizontalResizable>
        </div>
      )}
    </div>
  );
};
