import { systemMetricRouteLoader } from './systemMetric';
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
});
