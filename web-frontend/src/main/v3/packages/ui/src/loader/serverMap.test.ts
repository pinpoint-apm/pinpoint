import { makeArgs } from './__fixtures__/loaderArgs';
import { serverMapRouteLoader } from './serverMap';

jest.mock('react-router', () => ({
  redirect: (url: string) => ({ __isRedirect: true, url }),
}));

// servicemap이 꺼진 상태가 이 화면이 살아 있는 조건이다. 켜져 있으면 메뉴에서 감춰지므로
// 로더가 servicemap으로 옮긴다(맨 아래 describe).
jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  getConfiguration: jest.fn(() => Promise.resolve({})),
  getRequestService: jest.fn(() => 'DEFAULT'),
}));

import { getConfiguration, getRequestService } from '@pinpoint-fe/ui/src/hooks';

const APP = 'TestApp@SPRING_BOOT';
const BASE = `/serverMap/${APP}`;
const VALID = 'from=2023-11-10-14-30-00&to=2023-11-10-15-00-00';

describe('serverMapRouteLoader', () => {
  beforeEach(() => {
    (getConfiguration as jest.Mock).mockResolvedValue({});
    (getRequestService as jest.Mock).mockReturnValue('DEFAULT');
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('returns the application when from/to are in the canonical date format', async () => {
    const result = await serverMapRouteLoader(
      makeArgs(`http://localhost${BASE}?${VALID}`, { application: APP }),
    );
    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });

  test('redirects to the base path with default dates when no query params exist', async () => {
    const result = (await serverMapRouteLoader(
      makeArgs(`http://localhost${BASE}`, { application: APP }),
    )) as unknown as { __isRedirect: boolean; url: string };
    expect(result.__isRedirect).toBe(true);
    expect(result.url).toContain(BASE);
    expect(result.url).toContain('from=');
    expect(result.url).toContain('to=');
  });

  test('redirects when "from" is present but "to" is missing', async () => {
    const result = (await serverMapRouteLoader(
      makeArgs(`http://localhost${BASE}?from=2023-11-10-14-30-00`, { application: APP }),
    )) as unknown as { __isRedirect: boolean };
    expect(result.__isRedirect).toBe(true);
  });

  test('redirects when no whitelisted date format matches the range', async () => {
    const result = (await serverMapRouteLoader(
      makeArgs(`http://localhost${BASE}?from=not-a-date&to=also-not-a-date`, { application: APP }),
    )) as unknown as { __isRedirect: boolean };
    expect(result.__isRedirect).toBe(true);
  });

  test('returns the application when query params exist but "from" is absent', async () => {
    const result = await serverMapRouteLoader(
      makeArgs(`http://localhost${BASE}?bidirectional=true`, { application: APP }),
    );
    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });

  test('returns null when the application param is not a valid type@name', async () => {
    const result = await serverMapRouteLoader(
      makeArgs('http://localhost/serverMap/InvalidApp', { application: 'InvalidApp' }),
    );
    expect(result).toBeNull();
  });

  test('still resolves when configuration fetch fails', async () => {
    (getConfiguration as jest.Mock).mockRejectedValueOnce(new Error('backend down'));
    const result = await serverMapRouteLoader(
      makeArgs(`http://localhost${BASE}?${VALID}`, { application: APP }),
    );
    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });

  // servicemap이 켜지면 이 화면은 메뉴에서 감춰진다. URL로 직접 들어와도 그리지 않는다.
  describe('when serviceMap is enabled', () => {
    beforeEach(() => {
      (getConfiguration as jest.Mock).mockResolvedValue({
        'experimental.enableServiceMap.value': true,
      });
    });

    test('redirects to the servicemap page, adding the service segment', async () => {
      const result = (await serverMapRouteLoader(
        makeArgs(`http://localhost${BASE}?${VALID}`, { application: APP }),
      )) as unknown as { __isRedirect: boolean; url: string };

      expect(result.__isRedirect).toBe(true);
      expect(result.url).toBe(`/serviceMap/DEFAULT/${APP}?${VALID}`);
    });

    // 첫 진입 경로(`/` → `/serverMap`)도 이 갈래로 걸린다.
    test('redirects the bare servermap path too', async () => {
      const result = (await serverMapRouteLoader(
        makeArgs('http://localhost/serverMap', {}),
      )) as unknown as { __isRedirect: boolean; url: string };

      expect(result.url).toBe('/serviceMap/DEFAULT');
    });

    // DEFAULT가 아닌 service는 소속 application을 모두 모아 그려 기준 application이 없다.
    // 실어 보내면 목적지 로더가 곧 지우면서 한 번 더 움직인다.
    test('drops the application for a non-DEFAULT service', async () => {
      (getRequestService as jest.Mock).mockReturnValue('blogService');

      const result = (await serverMapRouteLoader(
        makeArgs(`http://localhost${BASE}?${VALID}`, { application: APP }),
      )) as unknown as { __isRedirect: boolean; url: string };

      expect(result.url).toBe(`/serviceMap/blogService?${VALID}`);
    });
  });
});
