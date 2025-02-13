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
import { useNavigate } from 'react-router-dom';
import { useSystemMetricSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { convertParamsToQueryString, getSystemMetricPath } from '@pinpoint-fe/ui/src/utils';
import { useTranslation } from 'react-i18next';
import { PiHardDrivesDuotone } from 'react-icons/pi';

export const SystemMetricPage = () => {
  const navigate = useNavigate();
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
          onClickHostGroup={(hostGroup) => navigate(getSystemMetricPath(hostGroup))}
        />
        <div className="ml-auto">
          {hostGroupName && (
            <DatetimePicker
              from={searchParameters.from}
              to={searchParameters.to}
              onChange={handleChangeDateRagePicker}
              maxDateRangeDays={28}
              outOfDateRangeMessage={t('DATE_RANGE_PICKER.MAX_SEARCH_PERIOD', {
                maxSearchPeriod: 28,
              })}
              timeUnits={['5m', '20m', '1h', '12h', '1d', '7d', '14d', '28d']}
            />
          )}
        </div>
      </MainHeader>
      {hostGroupName && (
        <LayoutWithContentSidebar>
          <SystemMetricSidebar />
          <SystemMetricChartList emptyMessage={t('COMMON.NO_DATA')} />
        </LayoutWithContentSidebar>
      )}
    </div>
  );
};
