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
import { useNavigate, useLocation } from 'react-router-dom';
import { useSystemMetricSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { convertParamsToQueryString, getSystemMetricPath } from '@pinpoint-fe/ui/src/utils';
import { useTranslation } from 'react-i18next';
import { PiHardDrivesDuotone } from 'react-icons/pi';
import { APP_SETTING_KEYS, Configuration } from '@pinpoint-fe/ui/src/constants';

export const SystemMetricPage = ({
  configuration,
}: {
  configuration?: Configuration & Record<string, unknown>;
}) => {
  const periodMax = configuration?.['periodMax.systemMetric'];
  const periodInterval = configuration?.['periodInterval.systemMetric'];
  const navigate = useNavigate();
  const location = useLocation();
  const { t } = useTranslation();
  const { searchParameters, hostGroupName, hostName } = useSystemMetricSearchParameters();

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
      {hostGroupName && (
        <LayoutWithContentSidebar autoSaveId={APP_SETTING_KEYS.SYSTEM_METRIC_RESIZABLE}>
          <SystemMetricSidebar />
          <SystemMetricChartList emptyMessage={t('COMMON.NO_DATA')} />
        </LayoutWithContentSidebar>
      )}
    </div>
  );
};
