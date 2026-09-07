import { EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';
import { resolveHiddenMapPageRedirect } from './hiddenMapPage';

jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  getConfiguration: jest.fn(() => Promise.resolve({})),
  getRequestService: jest.fn(() => 'DEFAULT'),
}));

import { getConfiguration, getRequestService } from '@pinpoint-fe/ui/src/hooks';

const APP = 'TestApp@SPRING_BOOT';
const PERIOD = 'from=2023-11-10-14-30-00&to=2023-11-10-15-00-00';

const resolve = (path: string) => resolveHiddenMapPageRedirect(`http://localhost${path}`);

/** configuration API가 내려주는 기본값. 사용자가 고른 값이 없으면 이 값이 이긴다. */
const setConfigured = (enableServiceMap: boolean) => {
  (getConfiguration as jest.Mock).mockResolvedValue({
    'experimental.enableServiceMap.value': enableServiceMap,
  });
};

describe('resolveHiddenMapPageRedirect', () => {
  beforeEach(() => {
    window.localStorage.clear();
    (getRequestService as jest.Mock).mockReturnValue('DEFAULT');
    setConfigured(false);
  });

  afterEach(() => {
    jest.clearAllMocks();
    window.localStorage.clear();
  });

  describe('serviceMap enabled — servermap is the hidden menu', () => {
    beforeEach(() => {
      setConfigured(true);
    });

    test('moves the servermap page to servicemap, keeping the application and the period', async () => {
      await expect(resolve(`/serverMap/${APP}?${PERIOD}`)).resolves.toBe(
        `/serviceMap/DEFAULT/${APP}?${PERIOD}`,
      );
    });

    // 첫 진입 경로(`/` → `/serverMap`)가 이 갈래로 걸린다.
    test('moves the bare servermap path', async () => {
      await expect(resolve('/serverMap')).resolves.toBe('/serviceMap/DEFAULT');
    });

    // `/serverMap/realtime`은 `/serverMap`의 하위 경로다. 실시간 보기를 먼저 봐야
    // 'realtime'을 application 세그먼트로 읽지 않는다.
    test('moves the realtime page to the servicemap realtime page', async () => {
      await expect(resolve(`/serverMap/realtime/${APP}`)).resolves.toBe(
        `/serviceMap/realtime/DEFAULT/${APP}`,
      );
    });

    test('moves the bare realtime path', async () => {
      await expect(resolve('/serverMap/realtime')).resolves.toBe('/serviceMap/realtime/DEFAULT');
    });

    // DEFAULT가 아닌 service는 소속 application을 모두 모아 그려 기준 application이 없다.
    // 실어 보내면 목적지 로더가 곧 지우면서 한 번 더 움직인다.
    test('drops the application for a non-DEFAULT service', async () => {
      (getRequestService as jest.Mock).mockReturnValue('blogService');

      await expect(resolve(`/serverMap/${APP}?${PERIOD}`)).resolves.toBe(
        `/serviceMap/blogService?${PERIOD}`,
      );
    });

    test('encodes a service name that would break the path', async () => {
      (getRequestService as jest.Mock).mockReturnValue('team/a');

      await expect(resolve('/serverMap')).resolves.toBe('/serviceMap/team%2Fa');
    });

    // 목적지에서 다시 걸리면 리다이렉트가 되돌아온다.
    test('leaves the servicemap pages alone', async () => {
      await expect(resolve(`/serviceMap/DEFAULT/${APP}?${PERIOD}`)).resolves.toBeUndefined();
      await expect(resolve(`/serviceMap/realtime/DEFAULT/${APP}`)).resolves.toBeUndefined();
    });

    test('leaves other pages alone', async () => {
      await expect(resolve(`/filteredMap/DEFAULT/${APP}`)).resolves.toBeUndefined();
      await expect(resolve(`/inspector/${APP}`)).resolves.toBeUndefined();
    });
  });

  describe('serviceMap disabled — servicemap is the hidden menu', () => {
    test('moves the servicemap page to servermap, dropping the service segment', async () => {
      await expect(resolve(`/serviceMap/DEFAULT/${APP}?${PERIOD}`)).resolves.toBe(
        `/serverMap/${APP}?${PERIOD}`,
      );
    });

    // servermap도 같은 기간을 보여줘야 한다. 떨어뜨리면 로더가 기본 기간으로 채운다.
    test('keeps the period when the service carries no application', async () => {
      await expect(resolve(`/serviceMap/blogService?${PERIOD}`)).resolves.toBe(
        `/serverMap?${PERIOD}`,
      );
    });

    test('moves the servicemap realtime page to the servermap realtime page', async () => {
      await expect(resolve(`/serviceMap/realtime/DEFAULT/${APP}`)).resolves.toBe(
        `/serverMap/realtime/${APP}`,
      );
    });

    // serviceName 세그먼트가 생기기 전 형태의 옛 링크. 첫 세그먼트가 application이다.
    test('reads a legacy path that carries only an application', async () => {
      await expect(resolve(`/serviceMap/${APP}`)).resolves.toBe(`/serverMap/${APP}`);
    });

    test('leaves the servermap pages alone', async () => {
      await expect(resolve(`/serverMap/${APP}?${PERIOD}`)).resolves.toBeUndefined();
      await expect(resolve(`/serverMap/realtime/${APP}`)).resolves.toBeUndefined();
    });
  });

  // 사용자가 Experimental 설정에서 고른 값이 configuration 기본값을 덮는다.
  describe('the stored value wins over the configured default', () => {
    test('stored true moves the servermap page even when configured false', async () => {
      setConfigured(false);
      window.localStorage.setItem(EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP, 'true');

      await expect(resolve('/serverMap')).resolves.toBe('/serviceMap/DEFAULT');
    });

    test('stored false moves the servicemap page even when configured true', async () => {
      setConfigured(true);
      window.localStorage.setItem(EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP, 'false');

      await expect(resolve(`/serviceMap/DEFAULT/${APP}`)).resolves.toBe(`/serverMap/${APP}`);
    });
  });

  // 어느 쪽이 보이는 메뉴인지 모르는 상태다. 모르는 채로 옮기면 백엔드가 잠깐 죽은 사이에
  // 들어온 링크의 URL이 반대쪽으로 바뀌어 버린다.
  describe('when the configuration cannot be read', () => {
    test('moves nothing', async () => {
      (getConfiguration as jest.Mock).mockRejectedValue(new Error('backend down'));

      await expect(resolve(`/serverMap/${APP}`)).resolves.toBeUndefined();
      await expect(resolve(`/serviceMap/DEFAULT/${APP}`)).resolves.toBeUndefined();
    });

    test('still honors a value the user picked', async () => {
      (getConfiguration as jest.Mock).mockRejectedValue(new Error('backend down'));
      window.localStorage.setItem(EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP, 'true');

      await expect(resolve(`/serverMap/${APP}`)).resolves.toBe(`/serviceMap/DEFAULT/${APP}`);
    });
  });
});
