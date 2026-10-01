import { makeArgs } from './__fixtures__/loaderArgs';
import { systemMetricRouteLoader } from './systemMetric';
import { APP_PATH } from '@pinpoint-fe/ui/src/constants';

jest.mock('react-router', () => ({
  // 경로에서 serviceName을 읽을 때 라우트 매칭(`matchRoutes`)을 쓰므로 실제 모듈을 둔다.
  ...jest.requireActual('react-router'),
  redirect: (url: string) => ({ __isRedirect: true, url }),
}));

jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  getConfiguration: jest.fn(() => Promise.resolve({})),
  // enableServiceMap이 꺼진 상태 — service 개념이 없다.
  getRequestService: jest.fn(() => undefined),
  getServices: jest.fn(() => Promise.resolve(['DEFAULT', 'svc'])),
}));

import { getDefaultStore } from 'jotai';
import { configurationAtom, selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';
import { getConfiguration, getRequestService, getServices } from '@pinpoint-fe/ui/src/hooks';

const store = getDefaultStore();

const HOST_GROUP = 'my-host-group';
const BASE = `${APP_PATH.SYSTEM_METRIC}/${HOST_GROUP}`;
const VALID = 'from=2023-11-10-14-30-00&to=2023-11-10-15-00-00';

describe('systemMetricRouteLoader', () => {
  beforeEach(() => {
    (getConfiguration as jest.Mock).mockResolvedValue({});
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('returns the hostGroup when from/to are valid canonical dates', async () => {
    const result = await systemMetricRouteLoader(
      makeArgs(`http://localhost${BASE}?${VALID}`, { hostGroup: HOST_GROUP }),
    );
    expect(result).toBe(HOST_GROUP);
  });

  test('redirects to the base path with default dates when no query params exist', async () => {
    const result = (await systemMetricRouteLoader(
      makeArgs(`http://localhost${BASE}`, { hostGroup: HOST_GROUP }),
    )) as unknown as { __isRedirect: boolean; url: string };
    expect(result.__isRedirect).toBe(true);
    expect(result.url).toContain(BASE);
    expect(result.url).toContain('from=');
    expect(result.url).toContain('to=');
  });

  test('redirects when the date range exceeds the allowed period', async () => {
    const result = (await systemMetricRouteLoader(
      makeArgs(`http://localhost${BASE}?from=2020-01-01-00-00-00&to=2023-11-10-15-00-00`, {
        hostGroup: HOST_GROUP,
      }),
    )) as unknown as { __isRedirect: boolean };
    expect(result.__isRedirect).toBe(true);
  });

  test('returns null when no hostGroup param is provided', async () => {
    const result = await systemMetricRouteLoader(
      makeArgs(`http://localhost${APP_PATH.SYSTEM_METRIC}`, {}),
    );
    expect(result).toBeNull();
  });

  test('still redirects with defaults when configuration fetch fails', async () => {
    (getConfiguration as jest.Mock).mockRejectedValueOnce(new Error('backend down'));
    const result = (await systemMetricRouteLoader(
      makeArgs(`http://localhost${BASE}`, { hostGroup: HOST_GROUP }),
    )) as unknown as { __isRedirect: boolean; url: string };
    expect(result.__isRedirect).toBe(true);
    expect(result.url).toContain(BASE);
  });

  describe('serviceName segment', () => {
    const DATES = 'from=2023-11-10-14-30-00&to=2023-11-10-15-00-00';

    beforeEach(() => {
      jest.useFakeTimers().setSystemTime(new Date('2023-11-10T15:00:00'));
      (getRequestService as jest.Mock).mockReturnValue('svc');
      (getServices as jest.Mock).mockResolvedValue(['DEFAULT', 'svc', 'a/b']);
    });

    afterEach(() => {
      jest.useRealTimers();
      (getRequestService as jest.Mock).mockReturnValue(undefined);
    });

    test('fills in the current service on the bare page path', async () => {
      expect(await systemMetricRouteLoader(makeArgs('http://localhost/systemMetric'))).toEqual({
        __isRedirect: true,
        url: '/systemMetric/svc',
      });
    });

    test('moves a host group only path under the current service', async () => {
      const result = await systemMetricRouteLoader(
        makeArgs(`http://localhost/systemMetric/${HOST_GROUP}?${DATES}`),
      );
      expect(result).toEqual({
        __isRedirect: true,
        url: `/systemMetric/svc/${HOST_GROUP}?${DATES}`,
      });
    });

    test('keeps a service only path when the segment is a known service', async () => {
      expect(await systemMetricRouteLoader(makeArgs('http://localhost/systemMetric/a%2Fb'))).toBe(
        null,
      );
    });

    // 목록을 못 읽으면 hostGroup으로 단정해 옮기지 않는다.
    test('keeps a single segment as is when the service list cannot be read', async () => {
      (getServices as jest.Mock).mockRejectedValueOnce(new Error('backend down'));
      expect(
        await systemMetricRouteLoader(makeArgs(`http://localhost/systemMetric/${HOST_GROUP}`)),
      ).toBeNull();
    });

    test('returns the host group read after the service segment', async () => {
      const result = await systemMetricRouteLoader(
        makeArgs(`http://localhost/systemMetric/a%2Fb/${HOST_GROUP}?${DATES}`),
      );
      expect(result).toBe(HOST_GROUP);
      expect(getServices).not.toHaveBeenCalled();
    });

    test('keeps the service segment in the date redirect', async () => {
      const result = (await systemMetricRouteLoader(
        makeArgs(`http://localhost/systemMetric/a%2Fb/${HOST_GROUP}`),
      )) as unknown as { __isRedirect: boolean; url: string };
      expect(result.__isRedirect).toBe(true);
      expect(result.url).toContain(`/systemMetric/a%2Fb/${HOST_GROUP}?from=`);
    });

    // 첫 로드에서는 라우터가 렌더한 경로가 없어 `getRequestService`가 주소창, 즉 이 옛 경로 자체를
    // 읽는다. 그 경로로 판정하면 hostGroup을 service로 읽어 `/systemMetric/{hostGroup}/{hostGroup}`이
    // 된다. 그래서 여기서는 mock이 아니라 실제 규칙으로 확인한다.
    describe('on a cold load with the real service rule', () => {
      beforeEach(() => {
        const { getRequestService: actualGetRequestService } = jest.requireActual(
          '@pinpoint-fe/ui/src/hooks/api/serviceNameFetchInterceptor',
        );
        (getRequestService as jest.Mock).mockImplementation(actualGetRequestService);
        store.set(configurationAtom, {
          'experimental.enableServiceMap.value': true,
        } as unknown as Configuration);
        store.set(selectedServiceAtom, 'svc');
        window.history.replaceState({}, '', `/systemMetric/${HOST_GROUP}?${DATES}`);
      });

      afterEach(() => {
        store.set(configurationAtom, undefined);
        store.set(selectedServiceAtom, 'DEFAULT');
        window.history.replaceState({}, '', '/');
      });

      test('moves an old host group bookmark under the selected service', async () => {
        const result = await systemMetricRouteLoader(
          makeArgs(`http://localhost/systemMetric/${HOST_GROUP}?${DATES}`),
        );
        expect(result).toEqual({
          __isRedirect: true,
          url: `/systemMetric/svc/${HOST_GROUP}?${DATES}`,
        });
      });

      test('fills in the selected service on the bare page path', async () => {
        window.history.replaceState({}, '', '/systemMetric');
        expect(await systemMetricRouteLoader(makeArgs('http://localhost/systemMetric'))).toEqual({
          __isRedirect: true,
          url: '/systemMetric/svc',
        });
      });
    });
  });

  // 설정이 켜진 채 만들어진 링크를 꺼진 상태에서 열어도 hostGroup을 읽는다.
  test('reads the last segment as the host group when the service map is disabled', async () => {
    const result = await systemMetricRouteLoader(
      makeArgs(`http://localhost/systemMetric/svc/${HOST_GROUP}?${VALID}`),
    );
    expect(result).toBe(HOST_GROUP);
  });
});
