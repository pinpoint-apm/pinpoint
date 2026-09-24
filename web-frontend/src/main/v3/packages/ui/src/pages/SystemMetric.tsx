import React from 'react';
import {
  DatetimePicker,
  DatetimePickerChangeHandler,
  MainHeader,
  HostGroupList,
  SystemMetricSidebar,
  SystemMetricChartList,
  LayoutWithContentSidebar,
} from '../components';
import { useNavigate, useLocation } from 'react-router';
import {
  useConfiguration,
  useIsForbiddenPath,
  useSystemMetricSearchParameters,
} from '@pinpoint-fe/ui/src/hooks';
import { Forbidden403 } from './Forbidden403';
import { convertParamsToQueryString, getSystemMetricPath } from '@pinpoint-fe/ui/src/utils';
import { useTranslation } from 'react-i18next';
import { PiHardDrivesDuotone } from 'react-icons/pi';
import { APP_SETTING_KEYS } from '@pinpoint-fe/ui/src/constants';

export const SystemMetricPage = () => {
  const configuration = useConfiguration();
  const periodMax = configuration?.['periodMax.systemMetric'];
  const periodInterval = configuration?.['periodInterval.systemMetric'];
  const navigate = useNavigate();
  const location = useLocation();
  const { t } = useTranslation();
  const { searchParameters, hostGroupName, hostName } = useSystemMetricSearchParameters();
  const isForbidden = useIsForbiddenPath();

  const handleChangeDateRagePicker = React.useCallback(
    (({ formattedDates: formattedDate }) => {
      navigate(
        `${getSystemMetricPath(hostGroupName)}?${convertParamsToQueryString({
          ...formattedDate,
          ...{ hostName },
        })}`,
      );
    }) as DatetimePickerChangeHandler,
    [hostGroupName, hostName],
  );

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        title={
          <div className="flex items-center gap-2">
            <PiHardDrivesDuotone /> System Metric
          </div>
        }
      >
        <HostGroupList
          open={!hostGroupName}
          selectedHostGroup={hostGroupName}
          selectPlaceHolder={t('METRIC.SELECT_HOST_GROUP')}
          inputPlaceHolder={t('METRIC.INPUT_HOST_GROUP')}
          onClickHostGroup={(hostGroup) => {
            const targetPath = getSystemMetricPath(hostGroup);
            if (location.pathname !== targetPath) {
              navigate(targetPath);
            }
          }}
        />
        <div className="ml-auto">
          {hostGroupName && (
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
          남은 조회들도 함께 멈춘다. 지금 이 화면이 부르는 API에는 권한 검사가 없지만, 나중에
          붙었을 때 조용히 빠지지 않도록 다른 화면과 같은 처리를 둔다.
          (어느 화면이 이 판정에서 빠지는지는 `coversPageOnForbidden`. 이슈 #10744) */}
      {isForbidden && <Forbidden403 />}
      {!isForbidden && hostGroupName && (
        <LayoutWithContentSidebar autoSaveId={APP_SETTING_KEYS.SYSTEM_METRIC_RESIZABLE}>
          <SystemMetricSidebar />
          <SystemMetricChartList emptyMessage={t('COMMON.NO_DATA')} />
        </LayoutWithContentSidebar>
      )}
    </div>
  );
};
