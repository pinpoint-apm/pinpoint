import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useInspectorSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { useTranslation } from 'react-i18next';
import {
  DatetimePicker,
  DatetimePickerChangeHandler,
  LayoutWithContentSidebar,
  MainHeader,
  InspectorSidebar,
  InspectorApplicationChartList,
  InspectorAgentChartList,
  InspectorAgentInfo,
  InspectorAgentStatusTimeline,
  InspectorApplicationStatusTimeline,
  ApplicationCombinedList,
  ApplicationCombinedListProps,
} from '../components';
import { convertParamsToQueryString, getInspectorPath } from '@pinpoint-fe/ui/src/utils';
import { PiChartLineDuotone } from 'react-icons/pi';
import { TimeUnitFormat } from '@pinpoint-fe/datetime-picker';
import { APP_SETTING_KEYS, Configuration } from '@pinpoint-fe/ui/src/constants';

export interface InspectorPageProps {
  configuration?: Configuration;
  ApplicationList?: (props: ApplicationCombinedListProps) => JSX.Element;
}

export const InspectorPage = ({
  configuration,
  ApplicationList = ApplicationCombinedList,
}: InspectorPageProps) => {
  const periodMax = configuration?.['periodMax.inspector'];
  const periodInterval = configuration?.['periodInterval.inspector'];
  const navigate = useNavigate();
  const { searchParameters, application, agentId, version } = useInspectorSearchParameters();
  const { t } = useTranslation();

  const handleChangeDateRagePicker = React.useCallback(
    (({ formattedDates: formattedDate }) => {
      navigate(
        `${getInspectorPath(application!)}?${convertParamsToQueryString({
          ...formattedDate,
          ...{ agentId, version },
        })}`,
      );
    }) as DatetimePickerChangeHandler,
    [application?.applicationName, agentId],
  );

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        title={
          <div className="flex items-center gap-2">
            <PiChartLineDuotone />
            <span className="relative">
              Inspector <span className="relative text-xs italic text-primary -top-2">Beta</span>
            </span>
          </div>
        }
      >
        <ApplicationList
          open={!application}
          selectedApplication={application}
          onClickApplication={(application) =>
            navigate(
              `${getInspectorPath(application)}?${convertParamsToQueryString({
                version,
              })}`,
            )
          }
        />
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
              timeUnits={periodInterval as TimeUnitFormat[]}
            />
          )}
        </div>
      </MainHeader>
      {application && (
        <LayoutWithContentSidebar
          contentWrapperClassName="h-fit"
          autoSaveId={APP_SETTING_KEYS.INSPECTOR_RESIZABLE}
        >
          <InspectorSidebar />
          {agentId ? (
            <div className="space-y-3">
              <InspectorAgentStatusTimeline />
              <InspectorAgentInfo />
              <InspectorAgentChartList emptyMessage={t('COMMON.NO_DATA')} />
            </div>
          ) : (
            <div className="space-y-3">
              <InspectorApplicationStatusTimeline />
              <InspectorApplicationChartList emptyMessage={t('COMMON.NO_DATA')} />
            </div>
          )}
        </LayoutWithContentSidebar>
      )}
    </div>
  );
};
