import React from 'react';
import { useLocation } from 'react-router';
import { addMilliseconds } from 'date-fns';
import { getApplicationTypeAndName } from '@pinpoint-fe/ui/src/utils';
import { getSearchParameters, getDateRange, getRealtimeDateRange } from './utils';

export const useServerMapSearchParameters = () => {
  const regex =
    /^(\/serverMap\/realtime|\/serviceMap\/realtime|\/scatterFullScreenMode\/realtime|\/heatmapFullScreenMode\/realtime)/;
  const { search, pathname } = useLocation();
  const searchParameters = getSearchParameters(search);
  const queryOption = getServerMapQueryOption(searchParameters);
  const application = getApplicationTypeAndName(pathname);

  const isRealtime = regex.test(pathname);
  const dateRange = { ...getDateRange(search, false), isRealtime };
  const [realtimeDateRange, setRealtimeDateRange] = React.useState(() => ({
    ...getDateRange(search, isRealtime),
    isRealtime,
  }));

  // 클린업으로 끄는 이유: 화면을 떠날 때(unmount)도 타이머를 거둬야 한다. 실시간 화면을
  // 드나들 때마다 하나씩 남으면 사라진 컴포넌트의 state를 계속 갱신한다.
  React.useEffect(() => {
    if (!isRealtime) {
      return;
    }

    // 마운트할 때는 초기값이 이미 방금 만든 창이라 다시 만들지 않는다. 다시 만들면 몇 ms 차이로
    // 조회 조건이 달라져 같은 화면을 두 번 조회한다. 실시간이 도중에 켜졌을 때만(비실시간 경로에서
    // 넘어와 컴포넌트가 그대로 남은 경우) 창을 새로 잡는다.
    setRealtimeDateRange((prev) =>
      prev.isRealtime ? prev : { ...getRealtimeDateRange(), isRealtime: true },
    );
    const interval = setInterval(() => {
      setRealtimeDateRange((prev) => ({
        isRealtime: true,
        from: addMilliseconds(prev.from, 2000),
        to: addMilliseconds(prev.to, 2000),
      }));
    }, 2000);

    return () => clearInterval(interval);
  }, [isRealtime]);
  return {
    search,
    pathname,
    dateRange: isRealtime ? realtimeDateRange : dateRange,
    searchParameters,
    application,
    queryOption,
  };
};

const getServerMapQueryOption = (searchParameters: { [k: string]: string }) => {
  const inbound = searchParameters?.inbound ? parseInt(searchParameters?.inbound, 10) : 1;
  const outbound = searchParameters?.outbound ? parseInt(searchParameters?.outbound, 10) : 1;
  const wasOnly = searchParameters?.wasOnly ? searchParameters?.wasOnly === 'true' : false;
  const bidirectional = searchParameters?.bidirectional
    ? searchParameters?.bidirectional === 'true'
    : false;

  return {
    inbound,
    outbound,
    bidirectional,
    wasOnly,
  };
};
