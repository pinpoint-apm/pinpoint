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

export interface InspectorPageProps {
  ApplicationList?: (props: ApplicationCombinedListProps) => JSX.Element;
}

export const InspectorPage = ({
  ApplicationList = ApplicationCombinedList,
}: InspectorPageProps) => {
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
              maxDateRangeDays={14}
              outOfDateRangeMessage={t('DATE_RANGE_PICKER.MAX_SEARCH_PERIOD', {
                maxSearchPeriod: 14,
              })}
              timeUnits={['5m', '20m', '1h', '12h', '1d', '7d', '14d']}
            />
          )}
        </div>
      </MainHeader>
      {application && (
        <LayoutWithContentSidebar contentWrapperClassName="h-fit">
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
