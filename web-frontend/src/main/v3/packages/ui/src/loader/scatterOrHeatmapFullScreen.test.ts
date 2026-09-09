import { makeArgs } from './__fixtures__/loaderArgs';
import {
  scatterOrHeatmapFullScreenLoader,
  scatterOrHeatmapFullScreenRealtimeLoader,
} from './scatterOrHeatmapFullScreen';

jest.mock('react-router', () => ({
  redirect: (url: string) => ({ __isRedirect: true, url }),
}));

jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  getConfiguration: jest.fn(() => Promise.resolve({})),
}));

import { getConfiguration } from '@pinpoint-fe/ui/src/hooks';

const APP = 'TestApp@SPRING_BOOT';
// The loader derives the base path from the page prefix carried in the URL.
const PATHNAME = 'heatmapFullScreenMode';
const BASE = `/${PATHNAME}/${APP}`;
const SERVICE_SCOPED_BASE = `/${PATHNAME}/blogService/${APP}`;
const REALTIME_BASE = `/${PATHNAME}/realtime/blogService/${APP}`;
const VALID = 'from=2023-11-10-14-30-00&to=2023-11-10-15-00-00';

describe('scatterOrHeatmapFullScreenLoader', () => {
  beforeEach(() => {
    (getConfiguration as jest.Mock).mockResolvedValue({});
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('returns the application when from/to are valid canonical dates', async () => {
    const result = await scatterOrHeatmapFullScreenLoader(
      makeArgs(`http://localhost${BASE}?${VALID}`, { application: APP }),
    );
    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });

  test('redirects to the derived base path with default dates when no query params exist', async () => {
    const result = (await scatterOrHeatmapFullScreenLoader(
      makeArgs(`http://localhost${BASE}`, { application: APP }),
    )) as unknown as { __isRedirect: boolean; url: string };
    expect(result.__isRedirect).toBe(true);
    expect(result.url).toContain(BASE);
    expect(result.url).toContain('from=');
    expect(result.url).toContain('to=');
  });

  test('redirects when "from" is present but the range is invalid', async () => {
    const result = (await scatterOrHeatmapFullScreenLoader(
      makeArgs(`http://localhost${BASE}?from=2020-01-01-00-00-00&to=2023-11-10-15-00-00`, {
        application: APP,
      }),
    )) as unknown as { __isRedirect: boolean };
    expect(result.__isRedirect).toBe(true);
  });

  // 확대 버튼은 새 탭을 여는데, 그 화면도 같은 service로 조회해야 한다. 날짜를 고치는
  // 리다이렉트가 serviceName 세그먼트를 떨어뜨리면 그 순간 전역 선택값으로 폴백한다.
  test('keeps the service name segment in the redirect destination', async () => {
    const result = (await scatterOrHeatmapFullScreenLoader(
      makeArgs(`http://localhost${SERVICE_SCOPED_BASE}`, {
        serviceName: 'blogService',
        application: APP,
      }),
    )) as unknown as { __isRedirect: boolean; url: string };
    expect(result.__isRedirect).toBe(true);
    expect(result.url.startsWith(`${SERVICE_SCOPED_BASE}?`)).toBe(true);
  });

  // react-router의 params는 디코딩된 값이라 '%2F'가 '/'로 풀려 세그먼트 경계가 어긋난다.
  test('keeps the encoded service name segment as-is in the redirect destination', async () => {
    const result = (await scatterOrHeatmapFullScreenLoader(
      makeArgs(`http://localhost/${PATHNAME}/team%2Fa/${APP}`, {
        serviceName: 'team/a',
        application: APP,
      }),
    )) as unknown as { __isRedirect: boolean; url: string };
    expect(result.url.startsWith(`/${PATHNAME}/team%2Fa/${APP}?`)).toBe(true);
  });

  test('returns the application on a service scoped path with valid dates', async () => {
    const result = await scatterOrHeatmapFullScreenLoader(
      makeArgs(`http://localhost${SERVICE_SCOPED_BASE}?${VALID}`, {
        serviceName: 'blogService',
        application: APP,
      }),
    );
    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });

  test('redirects to root when the application param is invalid', async () => {
    const result = await scatterOrHeatmapFullScreenLoader(
      makeArgs(`http://localhost/${PATHNAME}/InvalidApp`, { application: 'InvalidApp' }),
    );
    expect(result).toEqual({ __isRedirect: true, url: '/' });
  });
});

describe('scatterOrHeatmapFullScreenRealtimeLoader', () => {
  afterEach(() => {
    jest.restoreAllMocks();
  });

  test('returns the application when only agentId is present', () => {
    const result = scatterOrHeatmapFullScreenRealtimeLoader(
      makeArgs(`http://localhost${BASE}?agentId=agent-1`, { application: APP }),
    );
    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });

  test('redirects to the clean realtime URL preserving agentId when extra params exist', () => {
    const result = scatterOrHeatmapFullScreenRealtimeLoader(
      makeArgs(`http://localhost${BASE}?agentId=agent-1&from=x`, { application: APP }),
    );
    expect(result).toEqual({
      __isRedirect: true,
      url: `${BASE}?agentId=agent-1`,
    });
  });

  // 예전에는 리다이렉트가 첫 세그먼트만으로 경로를 다시 만들어 '/realtime'을 떨어뜨렸다.
  // 실시간 화면에서 기간 파라미터가 섞여 들어오면 비실시간 화면으로 튕겨 나갔다.
  test('keeps the realtime segment and the service name in the redirect destination', () => {
    const result = scatterOrHeatmapFullScreenRealtimeLoader(
      makeArgs(`http://localhost${REALTIME_BASE}?agentId=agent-1&from=x`, {
        serviceName: 'blogService',
        application: APP,
      }),
    );
    expect(result).toEqual({
      __isRedirect: true,
      url: `${REALTIME_BASE}?agentId=agent-1`,
    });
  });

  test('returns null when an exception is thrown', () => {
    jest.spyOn(console, 'error').mockImplementation(() => {});
    const result = scatterOrHeatmapFullScreenRealtimeLoader(
      makeArgs('not-a-valid-url', { application: APP }),
    );
    expect(result).toBeNull();
  });
});
