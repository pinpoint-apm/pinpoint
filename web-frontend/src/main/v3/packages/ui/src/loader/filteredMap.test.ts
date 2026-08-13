import { filteredMapRouteLoader } from './filteredMap';

jest.mock('react-router-dom', () => ({
  redirect: (url: string) => ({ __isRedirect: true, url }),
}));

jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  getConfiguration: jest.fn(() => Promise.resolve({})),
}));

import { getConfiguration } from '@pinpoint-fe/ui/src/hooks';

const makeArgs = (url: string) => ({
  params: {},
  request: { url } as Request,
  context: {},
});

const APP = 'ACL-PORTAL-DEV@SPRING_BOOT';
const VALID = 'from=2023-11-10-14-30-00&to=2023-11-10-15-00-00';
const FILTER = 'filter=%5B%7B%22fa%22%3A%22APP%22%7D%5D';

type Redirect = { __isRedirect: boolean; url: string };

describe('filteredMapRouteLoader', () => {
  beforeEach(() => {
    (getConfiguration as jest.Mock).mockResolvedValue({});
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('a path carrying a service name (came from the servicemap)', () => {
    const BASE = `/filteredMap/DEFAULT/${APP}`;

    test('returns the application when from/to are already canonical', async () => {
      const result = await filteredMapRouteLoader(makeArgs(`http://localhost${BASE}?${VALID}`));

      expect(result).toEqual({
        applicationName: 'ACL-PORTAL-DEV',
        serviceType: 'SPRING_BOOT',
      });
    });

    // 날짜를 고치는 리다이렉트가 경로 형태를 바꾸면, 그 화면은 다른 service를 조회하게 된다.
    test('keeps the service segment when it fills in the dates', async () => {
      const result = (await filteredMapRouteLoader(
        makeArgs(`http://localhost${BASE}`),
      )) as unknown as Redirect;

      expect(result.__isRedirect).toBe(true);
      expect(result.url).toContain(`${BASE}?`);
      expect(result.url).not.toContain('/serverMap');
    });

    test('keeps an encoded service name encoded', async () => {
      const result = (await filteredMapRouteLoader(
        makeArgs(`http://localhost/filteredMap/team%2Fa/${APP}`),
      )) as unknown as Redirect;

      expect(result.url).toContain(`/filteredMap/team%2Fa/${APP}?`);
    });

    // filter가 없으면 filteredMap은 아무것도 그릴 수 없다. 날짜를 고치면서 떨어뜨리면 안 된다.
    test('keeps the filter when it rewrites the dates', async () => {
      const result = (await filteredMapRouteLoader(
        makeArgs(`http://localhost${BASE}?from=not-a-date&to=also-not-a-date&${FILTER}`),
      )) as unknown as Redirect;

      expect(result.__isRedirect).toBe(true);
      expect(result.url).toContain('filter=');
      expect(result.url).toMatch(/from=\d{4}-\d{2}-\d{2}-\d{2}-\d{2}-\d{2}/);
    });

    test('keeps the filter when "to" is missing', async () => {
      const result = (await filteredMapRouteLoader(
        makeArgs(`http://localhost${BASE}?from=2023-11-10-14-30-00&${FILTER}`),
      )) as unknown as Redirect;

      expect(result.__isRedirect).toBe(true);
      expect(result.url).toContain('filter=');
    });
  });

  describe('a path carrying only an application (came from the servermap)', () => {
    const BASE = `/filteredMap/${APP}`;

    test('returns the application without moving the path under a service', async () => {
      const result = await filteredMapRouteLoader(makeArgs(`http://localhost${BASE}?${VALID}`));

      expect(result).toEqual({
        applicationName: 'ACL-PORTAL-DEV',
        serviceType: 'SPRING_BOOT',
      });
    });

    // serviceName을 채워 넣으면 servermap에서 온 화면이 servicemap에서 온 것처럼 보인다.
    test('does not add a service segment when it fills in the dates', async () => {
      const result = (await filteredMapRouteLoader(
        makeArgs(`http://localhost${BASE}`),
      )) as unknown as Redirect;

      expect(result.__isRedirect).toBe(true);
      expect(result.url).toContain(`${BASE}?`);
    });

    // 예전에는 servermap 로더를 그대로 써서 이 리다이렉트가 `/serverMap/...`으로 가며
    // filter까지 떨어뜨렸다. 필터 없는 filteredMap은 아무것도 그리지 못한다.
    test('stays under /filteredMap and keeps the filter when it rewrites the dates', async () => {
      const result = (await filteredMapRouteLoader(
        makeArgs(`http://localhost${BASE}?from=not-a-date&to=also-not-a-date&${FILTER}`),
      )) as unknown as Redirect;

      expect(result.__isRedirect).toBe(true);
      expect(result.url).toContain(`${BASE}?`);
      expect(result.url).not.toContain('/serverMap');
      expect(result.url).toContain('filter=');
    });
  });

  // 그릴 map이 없으므로 날짜도 채우지 않는다.
  test('does not redirect when the path carries no application', async () => {
    expect(await filteredMapRouteLoader(makeArgs('http://localhost/filteredMap'))).toBeNull();
    expect(
      await filteredMapRouteLoader(makeArgs('http://localhost/filteredMap/blogService')),
    ).toBeNull();
  });

  test('still resolves when configuration fetch fails', async () => {
    (getConfiguration as jest.Mock).mockRejectedValueOnce(new Error('backend down'));

    const result = await filteredMapRouteLoader(
      makeArgs(`http://localhost/filteredMap/DEFAULT/${APP}?${VALID}`),
    );

    expect(result).toEqual({ applicationName: 'ACL-PORTAL-DEV', serviceType: 'SPRING_BOOT' });
  });
});
