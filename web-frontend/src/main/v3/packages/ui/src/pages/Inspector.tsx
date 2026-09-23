import React from 'react';
import { useNavigate } from 'react-router';
import {
  useConfiguration,
  useInspectorSearchParameters,
  useIsForbiddenPath,
} from '@pinpoint-fe/ui/src/hooks';
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
import { Forbidden403 } from './Forbidden403';
import { PiChartLineDuotone } from 'react-icons/pi';
import { TimeUnitFormat } from '@pinpoint-fe/datetime-picker';
import { APP_SETTING_KEYS } from '@pinpoint-fe/ui/src/constants';

export interface InspectorPageProps {
  ApplicationList?: (props: ApplicationCombinedListProps) => React.ReactElement;
}

export const InspectorPage = ({
  ApplicationList = ApplicationCombinedList,
}: InspectorPageProps) => {
  const configuration = useConfiguration();
  const isForbidden = useIsForbiddenPath();
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
      {/* 이 화면의 API 중 하나가 403을 받았다. **헤더는 남기고 본문만 바꾼다** — 본문을 언마운트해
          남은 조회들도 함께 멈추고, 사용자는 위의 선택 박스로 다른 대상을 고를 수 있다.
          (어느 화면이 이 판정에서 빠지는지는 `coversPageOnForbidden`. 이슈 #10744) */}
      {isForbidden && <Forbidden403 />}
      {!isForbidden && application && (
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
