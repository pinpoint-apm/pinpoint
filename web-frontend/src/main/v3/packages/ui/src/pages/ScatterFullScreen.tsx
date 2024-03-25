import React from 'react';
import { Link } from 'react-router-dom';
import {
  convertParamsToQueryString,
  getScatterFullScreenPath,
  getScatterFullScreenRealtimePath,
  getServerMapPath,
} from '@pinpoint-fe/utils';
import { useServerMapSearchParameters } from '@pinpoint-fe/hooks';
import { useNavigate } from 'react-router-dom';
import {
  ApplicationCombinedList,
  DatetimePicker,
  DatetimePickerChangeHandler,
  MainHeader,
  ScatterChart,
} from '../components';
import { FaNetworkWired } from 'react-icons/fa';
import { useTranslation } from 'react-i18next';

export const ScatterFullScreenPage = () => {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const { dateRange, application, searchParameters } = useServerMapSearchParameters();
  const isRealtime = dateRange.isRealtime;
  const agentId = searchParameters.agentId;

  const handleChangeDateRagePicker = React.useCallback(
    (({ formattedDates, isRealtime }) => {
      if (isRealtime) {
        navigate(`${getScatterFullScreenRealtimePath(application!)}`);
      } else {
        navigate(
          `${getScatterFullScreenPath(application!)}?${convertParamsToQueryString({
            ...formattedDates,
          })}`,
        );
      }
      // TODO
      // if (formattedDates) {
      //   const agentIdQueryString = agentId ? convertParamsToQueryString({ agentId }) : '';
      //   navigate(
      //     `${getScatterFullScreenPath(application!)}?${convertParamsToQueryString(formattedDates)}${
      //       agentIdQueryString ? '&' + agentIdQueryString : ''
      //     }`,
      //   );
      // }
    }) as DatetimePickerChangeHandler,
    [application?.applicationName, agentId],
  );

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        title={
          <div className="flex items-center gap-2">
            <FaNetworkWired />
            <span>
              <Link className="hover:underline" to={getServerMapPath(application)}>
                Servermap
              </Link>{' '}
              / Scatter
            </span>
          </div>
        }
      >
        <ApplicationCombinedList selectedApplication={application} disabled />
        <div className="flex items-center gap-1 ml-4 text-sm font-semibold truncate">
          <div className="truncate">({agentId ? agentId : 'all'})</div>
        </div>
        <div className="ml-auto">
          {application && (
            <DatetimePicker
              enableRealtimeButton
              from={searchParameters.from}
              to={searchParameters.to}
              onChange={handleChangeDateRagePicker}
              outOfDateRangeMessage={t('DATE_RANGE_PICKER.MAX_SEARCH_PERIOD', {
                maxSearchPeriod: 2,
              })}
              isRealtime={isRealtime}
            />
          )}
        </div>
      </MainHeader>
      <div className="relative flex items-center justify-center flex-1 overflow-x-hidden">
        <div className="max-w-7xl w-full p-10 aspect-[1.618]">
          {application && (
            <ScatterChart
              agentId={agentId}
              node={application}
              realtime={isRealtime}
              toolbarOption={{ expand: { hide: true } }}
            />
          )}
        </div>
      </div>
    </div>
  );
};
