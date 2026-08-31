import type { LoaderFunctionArgs } from 'react-router';
import { serviceMapRealtimeLoader } from './serviceMapRealtime';

jest.mock('react-router', () => ({
  redirect: (url: string) => ({ __isRedirect: true, url }),
}));

jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  getRequestService: jest.fn(() => 'DEFAULT'),
}));

import { getRequestService } from '@pinpoint-fe/ui/src/hooks';

const makeArgs = (url: string) => ({
  params: {},
  request: { url } as Request,
  url: new URL(url),
  pattern: '',
  context: {},
});

const APP = 'TestApp@SPRING_BOOT';
const BASE = `/serviceMap/realtime/DEFAULT/${APP}`;

type Redirect = { __isRedirect: boolean; url: string };

describe('serviceMapRealtimeLoader', () => {
  beforeEach(() => {
    (getRequestService as jest.Mock).mockReturnValue('DEFAULT');
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('returns the application on the canonical path', () => {
    expect(serviceMapRealtimeLoader(makeArgs(`http://localhost${BASE}`))).toEqual({
      applicationName: 'TestApp',
      serviceType: 'SPRING_BOOT',
    });
  });

  // 실시간 보기는 기간을 화면이 직접 만든다. 실려 들어온 기간은 의미가 없으므로 지운다.
  test('drops any query string', () => {
    const result = serviceMapRealtimeLoader(
      makeArgs(`http://localhost${BASE}?from=2023-11-10-15-00-00&to=2023-11-10-15-05-00`),
    ) as unknown as Redirect;

    expect(result.__isRedirect).toBe(true);
    expect(result.url).toBe(BASE);
  });

  // serviceName 세그먼트가 없던 형태의 링크·북마크. 지금 보고 있는 service를 붙여 표준 형태로 옮긴다.
  test('adds the service segment when the path carries only an application', () => {
    const result = serviceMapRealtimeLoader(
      makeArgs(`http://localhost/serviceMap/realtime/${APP}`),
    ) as unknown as Redirect;

    expect(result.__isRedirect).toBe(true);
    expect(result.url).toBe(BASE);
  });

  test('encodes a service name that would break the path', () => {
    (getRequestService as jest.Mock).mockReturnValue('team/a');

    const result = serviceMapRealtimeLoader(
      makeArgs(`http://localhost/serviceMap/realtime/${APP}`),
    ) as unknown as Redirect;

    // 'team/a'는 DEFAULT가 아니므로 기준 application이 없다. 세그먼트 인코딩만 확인한다.
    expect(result.url).toBe('/serviceMap/realtime/team%2Fa');
  });

  // 옛 링크이면서 현재 service가 DEFAULT가 아니면 "service를 붙인다"와 "application을 뗀다"가
  // 둘 다 걸린다. 한 번에 목적지로 가지 않으면 화면이 두 번 움직인다.
  test('moves a legacy link to its final path in one redirect', () => {
    (getRequestService as jest.Mock).mockReturnValue('blogService');

    const result = serviceMapRealtimeLoader(
      makeArgs(`http://localhost/serviceMap/realtime/${APP}`),
    ) as unknown as Redirect;

    expect(result.url).toBe('/serviceMap/realtime/blogService');
    expect(serviceMapRealtimeLoader(makeArgs(`http://localhost${result.url}`))).toBeNull();
  });

  // DEFAULT는 고를 대상이 있으므로 되돌리지 않는다. 화면이 application 선택 박스를 띄운다.
  test('stays on the page when DEFAULT carries no application yet', () => {
    expect(serviceMapRealtimeLoader(makeArgs('http://localhost/serviceMap/realtime/DEFAULT'))).toBe(
      null,
    );
  });

  // DEFAULT가 아닌 service는 소속 application을 모두 모아 그리므로 기준 application이 없다.
  test('renders a non-DEFAULT service without an application', () => {
    expect(
      serviceMapRealtimeLoader(makeArgs('http://localhost/serviceMap/realtime/blogService')),
    ).toBe(null);
  });

  // 다른 화면 링크를 타고 실려 들어온 application. 그대로 두면 클릭한 노드가 아니라
  // 그 application의 수치를 우측 패널에 보여준다.
  test('drops an application carried into a non-DEFAULT service', () => {
    const result = serviceMapRealtimeLoader(
      makeArgs(`http://localhost/serviceMap/realtime/blogService/${APP}`),
    ) as unknown as Redirect;

    expect(result.__isRedirect).toBe(true);
    expect(result.url).toBe('/serviceMap/realtime/blogService');
  });

  test('returns null when an exception is thrown', () => {
    jest.spyOn(console, 'error').mockImplementation(() => {});

    expect(
      serviceMapRealtimeLoader({
        params: {},
        request: { url: 'not-a-valid-url' } as unknown as Request,
        context: {},
      } as unknown as LoaderFunctionArgs),
    ).toBeNull();

    jest.restoreAllMocks();
  });
});
