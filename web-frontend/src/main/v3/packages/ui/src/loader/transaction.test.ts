import { transactionRouteLoader } from './transaction';
import { APP_PATH } from '@pinpoint-fe/ui/src/constants';

jest.mock('react-router-dom', () => ({
  redirect: (url: string) => ({ __isRedirect: true, url }),
}));

jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  getConfiguration: jest.fn(() => Promise.resolve({})),
}));

import { getConfiguration } from '@pinpoint-fe/ui/src/hooks';

const makeArgs = (url: string, params: Record<string, string> = {}) => ({
  params,
  request: { url } as Request,
  context: {},
});

const APP = 'TestApp@SPRING_BOOT';
const BASE = `${APP_PATH.TRANSACTION_LIST}/${APP}`;
const VALID = 'from=2023-11-10-14-30-00&to=2023-11-10-15-00-00';

describe('transactionRouteLoader', () => {
  beforeEach(() => {
    (getConfiguration as jest.Mock).mockResolvedValue({});
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('returns the application when from/to are valid canonical dates', async () => {
    const result = await transactionRouteLoader(
      makeArgs(`http://localhost${BASE}?${VALID}`, { application: APP }),
    );
    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });

  test('returns the service name carried as its own segment', async () => {
    const result = await transactionRouteLoader(
      makeArgs(`http://localhost${APP_PATH.TRANSACTION_LIST}/svc/${APP}?${VALID}`),
    );

    expect(result).toEqual({
      serviceName: 'svc',
      applicationName: 'TestApp',
      serviceType: 'SPRING_BOOT',
    });
  });

  test('keeps the service name segment in the redirect target', async () => {
    const result = (await transactionRouteLoader(
      makeArgs(`http://localhost${APP_PATH.TRANSACTION_LIST}/svc/${APP}`),
    )) as unknown as { __isRedirect: boolean; url: string };

    expect(result.__isRedirect).toBe(true);
    expect(result.url).toContain(`${APP_PATH.TRANSACTION_LIST}/svc/${APP}`);
  });

  // react-router는 params를 디코딩하므로 params.application에는 '%2F'가 '/'로 풀려 들어온다.
  // 그대로 쓰면 세그먼트 경계가 어긋나고 리다이렉트 경로에 원본 '/'가 실려 라우트가 깨진다.
  test('reads the raw segment so an encoded service name survives the redirect', async () => {
    const result = (await transactionRouteLoader(
      makeArgs(`http://localhost${APP_PATH.TRANSACTION_LIST}/a%2Fb/${APP}`),
    )) as unknown as { __isRedirect: boolean; url: string };

    expect(result.__isRedirect).toBe(true);
    expect(result.url).toContain(`${APP_PATH.TRANSACTION_LIST}/a%2Fb/${APP}`);
  });

  test('decodes the encoded service name when returning the application', async () => {
    const result = await transactionRouteLoader(
      makeArgs(`http://localhost${APP_PATH.TRANSACTION_LIST}/a%2Fb/${APP}?${VALID}`),
    );

    expect(result).toEqual({
      serviceName: 'a/b',
      applicationName: 'TestApp',
      serviceType: 'SPRING_BOOT',
    });
  });

  // serviceName 세그먼트가 생기기 전 형태. 첫 세그먼트를 service 이름으로 오해하면 안 된다.
  test('reads a path that carries only an application', async () => {
    const result = await transactionRouteLoader(makeArgs(`http://localhost${BASE}?${VALID}`));

    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });

  test('redirects to the base path with default dates when no query params exist', async () => {
    const result = (await transactionRouteLoader(
      makeArgs(`http://localhost${BASE}`, { application: APP }),
    )) as unknown as { __isRedirect: boolean; url: string };
    expect(result.__isRedirect).toBe(true);
    expect(result.url).toContain(BASE);
    expect(result.url).toContain('from=');
    expect(result.url).toContain('to=');
  });

  test('redirects when the date range exceeds the allowed period', async () => {
    const result = (await transactionRouteLoader(
      makeArgs(`http://localhost${BASE}?from=2020-01-01-00-00-00&to=2023-11-10-15-00-00`, {
        application: APP,
      }),
    )) as unknown as { __isRedirect: boolean };
    expect(result.__isRedirect).toBe(true);
  });

  test('redirects when only "from" is provided without "to"', async () => {
    const result = (await transactionRouteLoader(
      makeArgs(`http://localhost${BASE}?from=2023-11-10-14-30-00`, { application: APP }),
    )) as unknown as { __isRedirect: boolean };
    expect(result.__isRedirect).toBe(true);
  });

  test('returns null when the application param is not a valid type@name', async () => {
    const result = await transactionRouteLoader(
      makeArgs(`http://localhost${APP_PATH.TRANSACTION_LIST}/InvalidApp`, {
        application: 'InvalidApp',
      }),
    );
    expect(result).toBeNull();
  });

  test('still redirects with defaults when configuration fetch fails', async () => {
    (getConfiguration as jest.Mock).mockRejectedValueOnce(new Error('backend down'));
    const result = (await transactionRouteLoader(
      makeArgs(`http://localhost${BASE}`, { application: APP }),
    )) as unknown as { __isRedirect: boolean; url: string };
    expect(result.__isRedirect).toBe(true);
    expect(result.url).toContain(BASE);
  });
});
