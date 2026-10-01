import { makeArgs } from './__fixtures__/loaderArgs';
import { inspectorRouteLoader } from './inspector';
import { urlStatisticRouteLoader } from './urlStatistic';
import { errorAnalysisRouteLoader } from './errorAnalysis';
import { openTelemetryRouteLoader } from './openTelemetry';
import { threadDumpRouteLoader } from './threadDump';
import { systemMetricRouteLoader } from './systemMetric';

/**
 * 하위 경로(`BASE_PATH`)로 배포한 경우. 로더의 `request.url`에는 라우터 basename이 붙어 있으므로
 * 로더는 그것을 떼고 경로를 읽어야 한다. 떼지 않으면 세그먼트가 어긋나 application을 못 읽는다
 * (threadDump가 servermap으로 쫓겨나고, 다른 화면은 날짜 정규화를 건너뛰었다).
 *
 * 리다이렉트 목적지는 basename 없이 만든다 — react-router가 상대 경로 redirect에 basename을 붙인다.
 */
jest.mock('@pinpoint-fe/ui/src/constants', () => ({
  ...jest.requireActual('@pinpoint-fe/ui/src/constants'),
  BASE_PATH: '/pinpoint',
}));

jest.mock('react-router', () => ({
  // 경로에서 serviceName을 읽을 때 라우트 매칭(`matchRoutes`)을 쓰므로 실제 모듈을 둔다.
  ...jest.requireActual('react-router'),
  redirect: (url: string) => ({ __isRedirect: true, url }),
}));

jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  getConfiguration: jest.fn(() => Promise.resolve({})),
  // enableServiceMap이 꺼진 상태 — 설정이 꺼진 기존 사용자도 같은 경로를 탄다.
  getRequestService: jest.fn(() => undefined),
  getServices: jest.fn(() => Promise.resolve(['DEFAULT', 'svc'])),
}));

import { getRequestService } from '@pinpoint-fe/ui/src/hooks';

const DATES = 'from=2023-11-10-14-30-00&to=2023-11-10-15-00-00';
const APPLICATION = { applicationName: 'app', serviceType: 'TOMCAT' };

describe('loaders under a BASE_PATH', () => {
  beforeEach(() => {
    jest.useFakeTimers().setSystemTime(new Date('2023-11-10T15:00:00'));
  });

  afterEach(() => {
    jest.useRealTimers();
    jest.clearAllMocks();
    (getRequestService as jest.Mock).mockReturnValue(undefined);
  });

  test.each([
    ['inspector', inspectorRouteLoader],
    ['urlStatistic', urlStatisticRouteLoader],
    ['errorAnalysis', errorAnalysisRouteLoader],
    ['openTelemetryMetric', openTelemetryRouteLoader],
  ])('%s: reads the application after the base path', async (page, loader) => {
    expect(await loader(makeArgs(`http://localhost/pinpoint/${page}/app@TOMCAT?${DATES}`))).toEqual(
      APPLICATION,
    );
  });

  test('inspector: normalizes the dates to a path without the base path', async () => {
    const result = (await inspectorRouteLoader(
      makeArgs('http://localhost/pinpoint/inspector/app@TOMCAT'),
    )) as unknown as { __isRedirect: boolean; url: string };

    expect(result.__isRedirect).toBe(true);
    expect(result.url).toMatch(/^\/inspector\/app@TOMCAT\?from=/);
  });

  test('threadDump: opens instead of falling back to servermap', async () => {
    expect(
      await threadDumpRouteLoader(
        makeArgs('http://localhost/pinpoint/threadDump/app@TOMCAT?agentId=agent'),
      ),
    ).toEqual(APPLICATION);
  });

  test('systemMetric: reads the host group after the base path', async () => {
    expect(
      await systemMetricRouteLoader(makeArgs(`http://localhost/pinpoint/systemMetric/hg?${DATES}`)),
    ).toBe('hg');
  });

  describe('with the service map enabled', () => {
    beforeEach(() => {
      (getRequestService as jest.Mock).mockReturnValue('svc');
    });

    test('inspector: reads the service segment after the base path', async () => {
      expect(
        await inspectorRouteLoader(
          makeArgs(`http://localhost/pinpoint/inspector/svc/app@TOMCAT?${DATES}`),
        ),
      ).toEqual(APPLICATION);
    });

    test('inspector: fills in the service on a path without the base path', async () => {
      expect(
        await inspectorRouteLoader(
          makeArgs(`http://localhost/pinpoint/inspector/app@TOMCAT?${DATES}`),
        ),
      ).toEqual({ __isRedirect: true, url: `/inspector/svc/app@TOMCAT?${DATES}` });
    });

    test('systemMetric: moves an old host group bookmark under the service', async () => {
      expect(
        await systemMetricRouteLoader(
          makeArgs(`http://localhost/pinpoint/systemMetric/hg?${DATES}`),
        ),
      ).toEqual({ __isRedirect: true, url: `/systemMetric/svc/hg?${DATES}` });
    });
  });
});
