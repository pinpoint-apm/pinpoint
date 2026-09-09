import React from 'react';
import { Link } from 'react-router';
import {
  convertParamsToQueryString,
  getHeatmapFullScreenPath,
  getHeatmapFullScreenRealtimePath,
  getScatterFullScreenPath,
  getScatterFullScreenRealtimePath,
  getServerMapPath,
  getServiceMapPath,
  getServiceNameFromPath,
} from '@pinpoint-fe/ui/src/utils';
import { useConfiguration, useServerMapSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { useNavigate, useLocation } from 'react-router';
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

export const ScatterOrHeatmapFullScreenPage = () => {
  const configuration = useConfiguration();
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
  // 이 화면의 service는 경로에 실려 온 값 하나로 정해진다. 전역 선택값으로 폴백하지 않는다 —
  // 확대 버튼은 새 탭을 열기 때문에, 원래 탭에서 service를 바꾸면 이 화면이 조회하던 대상과
  // 어긋난다. (filteredMap이 "어느 map에서 왔는가"를 정하는 것과 같은 이유다.)
  //
  // react-router의 `params`는 디코딩된 값이라 serviceName 안의 '%2F'가 '/'로 풀려 세그먼트
  // 경계가 어긋나므로, 인코딩된 원본 경로에서 읽는다.
  const serviceName = getServiceNameFromPath(location.pathname);
  // 경로에 serviceName이 실려 있으면 servicemap에서 넘어온 화면이다. 돌아갈 map도 그쪽이어야
  // 한다. (servermap에서 넘어온 화면은 예전과 같이 servermap으로 돌아간다.)
  const parentMap = serviceName
    ? { title: 'Servicemap', path: getServiceMapPath(serviceName, application) }
    : { title: 'Servermap', path: getServerMapPath(application) };

  const handleChangeDateRagePicker = React.useCallback(
    (({ formattedDates, isRealtime }) => {
      if (isRealtime) {
        navigate(
          type === 'scatter'
            ? getScatterFullScreenRealtimePath(application!, undefined, serviceName)
            : getHeatmapFullScreenRealtimePath(application!, undefined, serviceName),
        );
      } else {
        navigate(
          `${
            type === 'scatter'
              ? getScatterFullScreenPath(application!, undefined, serviceName)
              : getHeatmapFullScreenPath(application!, undefined, serviceName)
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
    [application?.applicationName, agentId, serviceName],
  );

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        title={
          <div className="flex items-center gap-2">
            <PiTreeStructureDuotone />
            <span>
              <Link className="hover:underline" to={parentMap.path}>
                {parentMap.title}
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
