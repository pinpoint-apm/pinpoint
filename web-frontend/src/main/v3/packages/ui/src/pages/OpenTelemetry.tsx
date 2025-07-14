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
import { convertParamsToQueryString, getOpenTelemetryPath } from '@pinpoint-fe/ui/src/utils';
import { useOpenTelemetrySearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { SiOpentelemetry } from 'react-icons/si';
import { APP_SETTING_KEYS, Configuration } from '@pinpoint-fe/ui/src/constants';

export interface OpenTelemetryPageProps {
  configuration?: Configuration & Record<string, unknown>;
  ApplicationList?: (props: ApplicationCombinedListProps) => JSX.Element;
}

export const OpenTelemetryPage = ({
  configuration,
  ApplicationList = ApplicationCombinedList,
}: OpenTelemetryPageProps) => {
  const periodMax = configuration?.['periodMax.otlpMetric'];
  const periodInterval = configuration?.['periodInterval.otlpMetric'];
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
            <SiOpentelemetry />
            <span className="relative">
              OpenTelemetry{' '}
              <span className="relative text-xs italic text-primary -top-2">Beta</span>
            </span>
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
        <LayoutWithContentSidebar
          contentWrapperClassName="max-w-full"
          autoSaveId={APP_SETTING_KEYS.OPEN_TELEMETRY_METRIC_RESIZABLE}
        >
          <OpenTelemetrySidebar />
          <OpenTelemetryDashboard />
        </LayoutWithContentSidebar>
      )}
    </div>
  );
};
