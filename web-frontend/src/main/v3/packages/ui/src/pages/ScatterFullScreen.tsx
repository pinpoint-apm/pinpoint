import React from 'react';
import { Link } from 'react-router-dom';
import {
  convertParamsToQueryString,
  getScatterFullScreenPath,
  getScatterFullScreenRealtimePath,
  getServerMapPath,
} from '@pinpoint-fe/ui/utils';
import { useServerMapSearchParameters } from '@pinpoint-fe/ui/hooks';
import { useNavigate } from 'react-router-dom';
import {
  ApplicationCombinedList,
  DatetimePicker,
  DatetimePickerChangeHandler,
  MainHeader,
  ScatterChart,
} from '../components';
import { PiTreeStructureDuotone } from 'react-icons/pi';
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
            <PiTreeStructureDuotone />
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
      <div className="flex items-center justify-center flex-1 overflow-x-hidden ">
        <div className="relative max-w-7xl w-full p-10 aspect-[1.618]">
          {agentId && (
            <div className="absolute text-sm font-semibold top-4">Agent ID: {agentId}</div>
          )}

          {application && (
            <ScatterChart
              agentId={agentId}
              node={application}
              realtime={isRealtime}
              toolbarOption={{ expand: { hide: true } }}
            />
          )}
        </div>
        {/* </div> */}
      </div>
    </div>
  );
};
