import { makeArgs } from './__fixtures__/loaderArgs';
import { serviceMapRouteLoader } from './serviceMap';

jest.mock('react-router', () => ({
  redirect: (url: string) => ({ __isRedirect: true, url }),
}));

jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  getConfiguration: jest.fn(() => Promise.resolve({})),
  getRequestService: jest.fn(() => 'DEFAULT'),
}));

import { getConfiguration, getRequestService } from '@pinpoint-fe/ui/src/hooks';

const APP = 'TestApp@SPRING_BOOT';
const DEFAULT_BASE = `/serviceMap/DEFAULT/${APP}`;
const VALID = 'from=2023-11-10-14-30-00&to=2023-11-10-15-00-00';

describe('serviceMapRouteLoader', () => {
  beforeEach(() => {
    (getConfiguration as jest.Mock).mockResolvedValue({});
    (getRequestService as jest.Mock).mockReturnValue('DEFAULT');
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('DEFAULT service', () => {
    test('returns the application when from/to are in the canonical date format', async () => {
      const result = await serviceMapRouteLoader(
        makeArgs(`http://localhost${DEFAULT_BASE}?${VALID}`),
      );

      expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
    });

    test('redirects to the serviceMap path keeping both segments, not to serverMap', async () => {
      const result = (await serviceMapRouteLoader(
        makeArgs(`http://localhost${DEFAULT_BASE}`),
      )) as unknown as { __isRedirect: boolean; url: string };

      expect(result.__isRedirect).toBe(true);
      expect(result.url).toContain(`${DEFAULT_BASE}?`);
      expect(result.url).not.toContain('/serverMap');
    });

    test('redirects when "from" is present but "to" is missing', async () => {
      const result = (await serviceMapRouteLoader(
        makeArgs(`http://localhost${DEFAULT_BASE}?from=2023-11-10-14-30-00`),
      )) as unknown as { __isRedirect: boolean };

      expect(result.__isRedirect).toBe(true);
    });

    test('redirects when no whitelisted date format matches the range', async () => {
      const result = (await serviceMapRouteLoader(
        makeArgs(`http://localhost${DEFAULT_BASE}?from=not-a-date&to=also-not-a-date`),
      )) as unknown as { __isRedirect: boolean };

      expect(result.__isRedirect).toBe(true);
    });

    // DEFAULT는 application을 골라야 map을 그리므로, 고르기 전에는 날짜를 채우지 않는다.
    test('does not fill in dates until an application is picked', async () => {
      const result = await serviceMapRouteLoader(makeArgs('http://localhost/serviceMap/DEFAULT'));

      expect(result).toBeNull();
    });
  });

  describe('a service other than DEFAULT', () => {
    test('fills in dates without an application, since it draws the whole service', async () => {
      const result = (await serviceMapRouteLoader(
        makeArgs('http://localhost/serviceMap/blogService'),
      )) as unknown as { __isRedirect: boolean; url: string };

      expect(result.__isRedirect).toBe(true);
      expect(result.url).toContain('/serviceMap/blogService?');
      expect(result.url).toContain('from=');
      expect(result.url).toContain('to=');
    });

    test('does not redirect when the dates are already canonical', async () => {
      const result = await serviceMapRouteLoader(
        makeArgs(`http://localhost/serviceMap/blogService?${VALID}`),
      );

      expect(result).toBeNull();
    });

    test('keeps the application segment when one is present', async () => {
      const result = await serviceMapRouteLoader(
        makeArgs(`http://localhost/serviceMap/blogService/${APP}?${VALID}`),
      );

      expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
    });

    // serviceName에 '/'가 들어오면 인코딩된 채로 다뤄야 세그먼트 경계가 지켜진다.
    test('keeps an encoded service name encoded in the redirect', async () => {
      const result = (await serviceMapRouteLoader(
        makeArgs('http://localhost/serviceMap/team%2Fa'),
      )) as unknown as { url: string };

      expect(result.url).toContain('/serviceMap/team%2Fa?');
    });
  });

  // URL이 이 화면의 service를 결정하므로, serviceName이 빠진 경로는 표준 형태로 옮긴다.
  describe('when the serviceName segment is missing', () => {
    test('adds the current service to a bare /serviceMap', async () => {
      (getRequestService as jest.Mock).mockReturnValue('blogService');

      const result = (await serviceMapRouteLoader(
        makeArgs(`http://localhost/serviceMap?${VALID}`),
      )) as unknown as { __isRedirect: boolean; url: string };

      expect(result.__isRedirect).toBe(true);
      expect(result.url).toContain('/serviceMap/blogService');
      expect(result.url).toContain(VALID);
    });

    // serviceName이 오기 전 형태(`/serviceMap/{app}@{type}`)의 링크·북마크를 살린다.
    test('moves a legacy application-only path under the current service', async () => {
      (getRequestService as jest.Mock).mockReturnValue('blogService');

      const result = (await serviceMapRouteLoader(
        makeArgs(`http://localhost/serviceMap/${APP}?${VALID}`),
      )) as unknown as { __isRedirect: boolean; url: string };

      expect(result.__isRedirect).toBe(true);
      expect(result.url).toContain(`/serviceMap/blogService/${APP}`);
    });
  });

  test('still resolves when configuration fetch fails', async () => {
    (getConfiguration as jest.Mock).mockRejectedValueOnce(new Error('backend down'));

    const result = await serviceMapRouteLoader(
      makeArgs(`http://localhost${DEFAULT_BASE}?${VALID}`),
    );

    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });
});
