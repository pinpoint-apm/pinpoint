import { scatterFullScreenLoader, scatterFullScreenRealtimeLoader } from './scatterFullScreen';
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
const BASE = `${APP_PATH.SCATTER_FULL_SCREEN}/${APP}`;
const VALID = 'from=2023-11-10-14-30-00&to=2023-11-10-15-00-00';

describe('scatterFullScreenLoader', () => {
  beforeEach(() => {
    (getConfiguration as jest.Mock).mockResolvedValue({});
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('returns the application when from/to are valid canonical dates', async () => {
    const result = await scatterFullScreenLoader(
      makeArgs(`http://localhost${BASE}?${VALID}`, { application: APP }),
    );
    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });

  test('redirects to the base path with default dates when no query params exist', async () => {
    const result = (await scatterFullScreenLoader(
      makeArgs(`http://localhost${BASE}`, { application: APP }),
    )) as unknown as { __isRedirect: boolean; url: string };
    expect(result.__isRedirect).toBe(true);
    expect(result.url).toContain(BASE);
    expect(result.url).toContain('from=');
    expect(result.url).toContain('to=');
  });

  test('redirects when "from" is present but the range is invalid', async () => {
    const result = (await scatterFullScreenLoader(
      makeArgs(`http://localhost${BASE}?from=2020-01-01-00-00-00&to=2023-11-10-15-00-00`, {
        application: APP,
      }),
    )) as unknown as { __isRedirect: boolean };
    expect(result.__isRedirect).toBe(true);
  });

  test('returns the application when query params exist but "from" is absent', async () => {
    const result = await scatterFullScreenLoader(
      makeArgs(`http://localhost${BASE}?agentId=agent-1`, { application: APP }),
    );
    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });

  test('redirects to root when the application param is invalid', async () => {
    const result = (await scatterFullScreenLoader(
      makeArgs(`http://localhost${APP_PATH.SCATTER_FULL_SCREEN}/InvalidApp`, {
        application: 'InvalidApp',
      }),
    )) as unknown as { __isRedirect: boolean; url: string };
    expect(result).toEqual({ __isRedirect: true, url: '/' });
  });
});

describe('scatterFullScreenRealtimeLoader', () => {
  afterEach(() => {
    jest.restoreAllMocks();
  });

  test('returns the application when only agentId is present', () => {
    const result = scatterFullScreenRealtimeLoader(
      makeArgs(`http://localhost${APP_PATH.SCATTER_FULL_SCREEN_REALTIME}/${APP}?agentId=agent-1`, {
        application: APP,
      }),
    );
    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });

  test('redirects to the clean realtime URL preserving agentId when extra params exist', () => {
    const result = scatterFullScreenRealtimeLoader(
      makeArgs(
        `http://localhost${APP_PATH.SCATTER_FULL_SCREEN_REALTIME}/${APP}?agentId=agent-1&from=x`,
        { application: APP },
      ),
    );
    expect(result).toEqual({
      __isRedirect: true,
      url: `${APP_PATH.SCATTER_FULL_SCREEN_REALTIME}/${APP}?agentId=agent-1`,
    });
  });

  test('returns null when an exception is thrown', () => {
    jest.spyOn(console, 'error').mockImplementation(() => {});
    const result = scatterFullScreenRealtimeLoader({
      params: { application: APP },
      request: { url: 'not-a-valid-url' } as unknown as Request,
      context: {},
    });
    expect(result).toBeNull();
  });
});
