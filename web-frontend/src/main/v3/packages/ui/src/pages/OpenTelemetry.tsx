import React from 'react';
import { useNavigate } from 'react-router';
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
import {
  useConfiguration,
  useIsForbiddenPath,
  useOpenTelemetrySearchParameters,
} from '@pinpoint-fe/ui/src/hooks';
import { Forbidden403 } from './Forbidden403';
import { SiOpentelemetry } from 'react-icons/si';
import { APP_SETTING_KEYS } from '@pinpoint-fe/ui/src/constants';

export interface OpenTelemetryPageProps {
  ApplicationList?: (props: ApplicationCombinedListProps) => React.ReactElement;
}

export const OpenTelemetryPage = ({
  ApplicationList = ApplicationCombinedList,
}: OpenTelemetryPageProps) => {
  const configuration = useConfiguration();
  const periodMax = configuration?.['periodMax.otlpMetric'];
  const periodInterval = configuration?.['periodInterval.otlpMetric'];
  const navigate = useNavigate();
  const { t } = useTranslation();
  const { searchParameters, application, agentId } = useOpenTelemetrySearchParameters();
  const isForbidden = useIsForbiddenPath();

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
      {/* 이 화면의 API 중 하나가 403을 받았다. **헤더는 남기고 본문만 바꾼다** — 본문을 언마운트해
          남은 조회들도 함께 멈추고, 사용자는 위의 선택 박스로 다른 대상을 고를 수 있다.
          (어느 화면이 이 판정에서 빠지는지는 `coversPageOnForbidden`. 이슈 #10744) */}
      {isForbidden && <Forbidden403 />}
      {!isForbidden && application && (
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
