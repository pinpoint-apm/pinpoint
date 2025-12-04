import React from 'react';
import { useAtom, useAtomValue } from 'jotai';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  getServerMapPath,
  convertParamsToQueryString,
  getServerImagePath,
  getApplicationKey,
  getFormattedDateRange,
  getRealtimePath,
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
  Configuration,
} from '@pinpoint-fe/ui/src/constants';
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
  configuration?: Configuration & Record<string, string>;
  ApplicationList?: (props: ApplicationCombinedListProps) => React.ReactElement;
}

const SERVERMAP_CONTAINER_ID = 'server-map-main-container';

export const ServerMapPage = ({
  authorizationGuideUrl,
  configuration,
  ApplicationList = ApplicationCombinedList,
}: ServermapPageProps) => {
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

  const handleChangeDateRagePicker = React.useCallback(
    (({ formattedDates: formattedDate, isRealtime }) => {
      if (isRealtime) {
        navigate(`${getRealtimePath(application!)}`);
      } else {
        navigate(
          `${getServerMapPath(application!)}?${convertParamsToQueryString({
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

  // FilterWizard
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
              Servermap
              <HelpPopover helpKey="HELP_VIEWER.SERVER_MAP" />
            </div>
          </div>
        }
      >
        <ApplicationList
          open={!application}
          selectedApplication={application}
          onClickApplication={(application) => navigate(getServerMapPath(application))}
        />
        {application && (
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
      {application && (
        <div
          id={SERVERMAP_CONTAINER_ID}
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
                  <ServerMap
                    queryOption={queryOption}
                    onApplyChangedOption={(option) => {
                      navigate(
                        `${getServerMapPath(application)}?${convertParamsToQueryString({
                          ...getFormattedDateRange(dateRange),
                          ...option,
                        })}`,
                      );
                    }}
                    onClickMenuItem={handleClickMenuItem}
                  />
                </>
              )}
            </div>
            {({ currentPanelWidth, SERVER_LIST_WIDTH, resizeHandleWidth }) => (
              <ServerMapChartsBoard
                authorizationGuideUrl={authorizationGuideUrl}
                currentPanelWidth={currentPanelWidth}
                SERVER_LIST_WIDTH={SERVER_LIST_WIDTH}
                resizeHandleWidth={resizeHandleWidth}
                SERVERMAP_CONTAINER_ID={SERVERMAP_CONTAINER_ID}
                configuration={configuration}
              />
            )}
          </LayoutWithHorizontalResizable>
        </div>
      )}
    </div>
  );
};
