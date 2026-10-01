import { makeArgs } from './__fixtures__/loaderArgs';
import { openTelemetryRouteLoader } from './openTelemetry';
import { APP_PATH } from '@pinpoint-fe/ui/src/constants';

jest.mock('react-router', () => ({
  // 경로에서 serviceName을 읽을 때 라우트 매칭(`matchRoutes`)을 쓰므로 실제 모듈을 둔다.
  ...jest.requireActual('react-router'),
  redirect: (url: string) => ({ __isRedirect: true, url }),
}));

jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  getConfiguration: jest.fn(() => Promise.resolve({})),
  // enableServiceMap이 꺼진 상태 — serviceName 세그먼트를 붙이지 않는다.
  getRequestService: jest.fn(() => undefined),
}));

import { getConfiguration, getRequestService } from '@pinpoint-fe/ui/src/hooks';

const APP = 'TestApp@SPRING_BOOT';
const BASE = `${APP_PATH.OPEN_TELEMETRY_METRIC}/${APP}`;
const VALID = 'from=2023-11-10-14-30-00&to=2023-11-10-15-00-00';

describe('openTelemetryRouteLoader', () => {
  beforeEach(() => {
    (getConfiguration as jest.Mock).mockResolvedValue({});
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('returns the application when from/to are valid canonical dates', async () => {
    const result = await openTelemetryRouteLoader(
      makeArgs(`http://localhost${BASE}?${VALID}`, { application: APP }),
    );
    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });

  test('redirects to the base path with default dates when no query params exist', async () => {
    const result = (await openTelemetryRouteLoader(
      makeArgs(`http://localhost${BASE}`, { application: APP }),
    )) as unknown as { __isRedirect: boolean; url: string };
    expect(result.__isRedirect).toBe(true);
    expect(result.url).toContain(BASE);
    expect(result.url).toContain('from=');
    expect(result.url).toContain('to=');
  });

  test('redirects when the date range exceeds the allowed period', async () => {
    const result = (await openTelemetryRouteLoader(
      makeArgs(`http://localhost${BASE}?from=2020-01-01-00-00-00&to=2023-11-10-15-00-00`, {
        application: APP,
      }),
    )) as unknown as { __isRedirect: boolean };
    expect(result.__isRedirect).toBe(true);
  });

  test('redirects when only "from" is provided without "to"', async () => {
    const result = (await openTelemetryRouteLoader(
      makeArgs(`http://localhost${BASE}?from=2023-11-10-14-30-00`, { application: APP }),
    )) as unknown as { __isRedirect: boolean };
    expect(result.__isRedirect).toBe(true);
  });

  test('returns null when the application param is not a valid type@name', async () => {
    const result = await openTelemetryRouteLoader(
      makeArgs(`http://localhost${APP_PATH.OPEN_TELEMETRY_METRIC}/InvalidApp`, {
        application: 'InvalidApp',
      }),
    );
    expect(result).toBeNull();
  });

  test('still redirects with defaults when configuration fetch fails', async () => {
    (getConfiguration as jest.Mock).mockRejectedValueOnce(new Error('backend down'));
    const result = (await openTelemetryRouteLoader(
      makeArgs(`http://localhost${BASE}`, { application: APP }),
    )) as unknown as { __isRedirect: boolean; url: string };
    expect(result.__isRedirect).toBe(true);
    expect(result.url).toContain(BASE);
  });

  describe('serviceName segment', () => {
    const PAGE = APP_PATH.OPEN_TELEMETRY_METRIC;
    const DATES = 'from=2023-11-10-14-30-00&to=2023-11-10-15-00-00';

    beforeEach(() => {
      // 날짜 검증이 기간 제한에 걸리지 않도록 "지금"을 고정한다.
      jest.useFakeTimers().setSystemTime(new Date('2023-11-10T15:00:00'));
    });

    afterEach(() => {
      jest.useRealTimers();
      (getRequestService as jest.Mock).mockReturnValue(undefined);
    });

    test('fills in the current service when the path does not carry one', async () => {
      (getRequestService as jest.Mock).mockReturnValue('a/b');
      const result = (await openTelemetryRouteLoader(
        makeArgs(`http://localhost${PAGE}/TestApp@SPRING_BOOT?${DATES}`),
      )) as unknown as { __isRedirect: boolean; url: string };
      expect(result).toEqual({
        __isRedirect: true,
        url: `${PAGE}/a%2Fb/TestApp@SPRING_BOOT?${DATES}`,
      });
    });

    test('fills in the current service before an application is picked', async () => {
      (getRequestService as jest.Mock).mockReturnValue('svc');
      const result = await openTelemetryRouteLoader(makeArgs(`http://localhost${PAGE}`));
      expect(result).toEqual({ __isRedirect: true, url: `${PAGE}/svc` });
    });

    test('keeps the carried service in the date redirect', async () => {
      (getRequestService as jest.Mock).mockReturnValue('other');
      const result = (await openTelemetryRouteLoader(
        makeArgs(`http://localhost${PAGE}/a%2Fb/TestApp@SPRING_BOOT`),
      )) as unknown as { __isRedirect: boolean; url: string };
      expect(result.__isRedirect).toBe(true);
      expect(result.url).toContain(`${PAGE}/a%2Fb/TestApp@SPRING_BOOT?`);
    });

    test('returns the application when the path already carries the service', async () => {
      const result = await openTelemetryRouteLoader(
        makeArgs(`http://localhost${PAGE}/svc/TestApp@SPRING_BOOT?${DATES}`),
      );
      expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
      expect(getRequestService).not.toHaveBeenCalled();
    });

    test('returns the bare service path as is', async () => {
      expect(await openTelemetryRouteLoader(makeArgs(`http://localhost${PAGE}/svc`))).toBeNull();
    });
  });
});
