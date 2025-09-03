import React from 'react';
import { Link } from 'react-router-dom';
import {
  convertParamsToQueryString,
  getHeatmapFullScreenPath,
  getHeatmapFullScreenRealtimePath,
  getScatterFullScreenPath,
  getScatterFullScreenRealtimePath,
  getServerMapPath,
} from '@pinpoint-fe/ui/src/utils';
import { useServerMapSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { APP_PATH, Configuration } from '@pinpoint-fe/ui/src/constants';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  ApplicationCombinedList,
  DatetimePicker,
  DatetimePickerChangeHandler,
  MainHeader,
  ScatterChart,
} from '../components';
import { PiTreeStructureDuotone } from 'react-icons/pi';
import { useTranslation } from 'react-i18next';
import { capitalize } from 'lodash';
import { Heatmap } from '@pinpoint-fe/ui/src/components/Heatmap/Heatmap';

export const ScatterOrHeatmapFullScreenPage = ({
  configuration,
}: {
  configuration?: Configuration & Record<string, unknown>;
}) => {
  const periodMax = configuration?.[`periodMax.serverMap`];
  const periodInterval = configuration?.[`periodInterval.serverMap`];
  const navigate = useNavigate();
  const location = useLocation();
  const match = location.pathname?.match(/^\/[^/]+/);
  const type = match?.[0].includes(APP_PATH.SCATTER_FULL_SCREEN) ? 'scatter' : 'heatmap';

  const { t } = useTranslation();
  const { dateRange, application, searchParameters } = useServerMapSearchParameters();
  const isRealtime = dateRange.isRealtime;
  const agentId = searchParameters.agentId;

  const handleChangeDateRagePicker = React.useCallback(
    (({ formattedDates, isRealtime }) => {
      if (isRealtime) {
        navigate(
          type === 'scatter'
            ? getScatterFullScreenRealtimePath(application!)
            : getHeatmapFullScreenRealtimePath(application!),
        );
      } else {
        navigate(
          `${
            type === 'scatter'
              ? getScatterFullScreenPath(application!)
              : getHeatmapFullScreenPath(application!)
          }?${convertParamsToQueryString({
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
              / {capitalize(type)}
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
              maxDateRangeDays={periodMax}
              outOfDateRangeMessage={t('DATE_RANGE_PICKER.MAX_SEARCH_PERIOD', {
                maxSearchPeriod: periodMax,
              })}
              isRealtime={isRealtime}
              timeUnits={periodInterval}
            />
          )}
        </div>
      </MainHeader>
      <div className="flex items-center justify-center flex-1 overflow-x-hidden ">
        <div className="relative max-w-7xl w-full p-10 aspect-[1.618]">
          {agentId && (
            <div className="absolute text-sm font-semibold top-4">Agent ID: {agentId}</div>
          )}
          {application &&
            (type === 'scatter' ? (
              <ScatterChart
                agentId={agentId}
                node={application}
                realtime={isRealtime}
                toolbarOption={{ expand: { hide: true } }}
              />
            ) : (
              <Heatmap
                realtime={isRealtime}
                agentId={agentId}
                nodeData={application}
                toolbarOption={{ expand: { hide: true } }}
              />
            ))}
        </div>
        {/* </div> */}
      </div>
    </div>
  );
};
