import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  DatetimePicker,
  DatetimePickerChangeHandler,
  LayoutWithContentSidebar,
  MainHeader,
  ApplicationCombinedList,
  ApplicationCombinedListProps,
  OpenTelemetrySidebar,
  OpenTelemetryDashboard,
} from '../components';
import { convertParamsToQueryString, getOpenTelemetryPath } from '@pinpoint-fe/utils';
import { useOpenTelemetrySearchParameters } from '@pinpoint-fe/hooks';
import { PiChartBarHorizontalDuotone } from 'react-icons/pi';

export interface OpenTelemetryPageProps {
  ApplicationList?: (props: ApplicationCombinedListProps) => JSX.Element;
}

export const OpenTelemetryPage = ({
  ApplicationList = ApplicationCombinedList,
}: OpenTelemetryPageProps) => {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const { searchParameters, application, agentId } = useOpenTelemetrySearchParameters();

  const handleChangeDateRagePicker = React.useCallback(
    (({ formattedDates: formattedDate }) => {
      navigate(
        `${getOpenTelemetryPath(application!)}?${convertParamsToQueryString({
          ...formattedDate,
          ...{ agentId },
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
            <PiChartBarHorizontalDuotone /> Open Telemetry
          </div>
        }
      >
        <ApplicationList
          open={!application}
          selectedApplication={application}
          onClickApplication={(application) => navigate(getOpenTelemetryPath(application))}
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
        <LayoutWithContentSidebar contentWrapperClassName="max-w-full">
          <OpenTelemetrySidebar />
          <OpenTelemetryDashboard />
        </LayoutWithContentSidebar>
      )}
    </div>
  );
};
