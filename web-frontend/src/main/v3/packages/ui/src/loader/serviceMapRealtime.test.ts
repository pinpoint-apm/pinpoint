import { makeArgs } from './__fixtures__/loaderArgs';
import { serviceMapRealtimeLoader } from './serviceMapRealtime';

jest.mock('react-router', () => ({
  redirect: (url: string) => ({ __isRedirect: true, url }),
}));

// servicemap이 켜진 상태가 이 화면이 살아 있는 조건이다. 꺼져 있으면 메뉴에서 감춰지므로
// 로더가 servermap 실시간 보기로 옮긴다(맨 아래 테스트).
jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  getConfiguration: jest.fn(() => Promise.resolve({ 'experimental.enableServiceMap.value': true })),
  getRequestService: jest.fn(() => 'DEFAULT'),
}));

import { getConfiguration, getRequestService } from '@pinpoint-fe/ui/src/hooks';

const APP = 'TestApp@SPRING_BOOT';
const BASE = `/serviceMap/realtime/DEFAULT/${APP}`;

type Redirect = { __isRedirect: boolean; url: string };

describe('serviceMapRealtimeLoader', () => {
  beforeEach(() => {
    (getConfiguration as jest.Mock).mockResolvedValue({
      'experimental.enableServiceMap.value': true,
    });
    (getRequestService as jest.Mock).mockReturnValue('DEFAULT');
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('returns the application on the canonical path', async () => {
    expect(await serviceMapRealtimeLoader(makeArgs(`http://localhost${BASE}`))).toEqual({
      applicationName: 'TestApp',
      serviceType: 'SPRING_BOOT',
    });
  });

  // 실시간 보기는 기간을 화면이 직접 만든다. 실려 들어온 기간은 의미가 없으므로 지운다.
  test('drops any query string', async () => {
    const result = (await serviceMapRealtimeLoader(
      makeArgs(`http://localhost${BASE}?from=2023-11-10-15-00-00&to=2023-11-10-15-05-00`),
    )) as unknown as Redirect;

    expect(result.__isRedirect).toBe(true);
    expect(result.url).toBe(BASE);
  });

  // serviceName 세그먼트가 없던 형태의 링크·북마크. 지금 보고 있는 service를 붙여 표준 형태로 옮긴다.
  test('adds the service segment when the path carries only an application', async () => {
    const result = (await serviceMapRealtimeLoader(
      makeArgs(`http://localhost/serviceMap/realtime/${APP}`),
    )) as unknown as Redirect;

    expect(result.__isRedirect).toBe(true);
    expect(result.url).toBe(BASE);
  });

  test('encodes a service name that would break the path', async () => {
    (getRequestService as jest.Mock).mockReturnValue('team/a');

    const result = (await serviceMapRealtimeLoader(
      makeArgs(`http://localhost/serviceMap/realtime/${APP}`),
    )) as unknown as Redirect;

    // 'team/a'는 DEFAULT가 아니므로 기준 application이 없다. 세그먼트 인코딩만 확인한다.
    expect(result.url).toBe('/serviceMap/realtime/team%2Fa');
  });

  // 옛 링크이면서 현재 service가 DEFAULT가 아니면 "service를 붙인다"와 "application을 뗀다"가
  // 둘 다 걸린다. 한 번에 목적지로 가지 않으면 화면이 두 번 움직인다.
  test('moves a legacy link to its final path in one redirect', async () => {
    (getRequestService as jest.Mock).mockReturnValue('blogService');

    const result = (await serviceMapRealtimeLoader(
      makeArgs(`http://localhost/serviceMap/realtime/${APP}`),
    )) as unknown as Redirect;

    expect(result.url).toBe('/serviceMap/realtime/blogService');
    expect(await serviceMapRealtimeLoader(makeArgs(`http://localhost${result.url}`))).toBeNull();
  });

  // DEFAULT는 고를 대상이 있으므로 되돌리지 않는다. 화면이 application 선택 박스를 띄운다.
  test('stays on the page when DEFAULT carries no application yet', async () => {
    expect(
      await serviceMapRealtimeLoader(makeArgs('http://localhost/serviceMap/realtime/DEFAULT')),
    ).toBe(null);
  });

  // DEFAULT가 아닌 service는 소속 application을 모두 모아 그리므로 기준 application이 없다.
  test('renders a non-DEFAULT service without an application', async () => {
    expect(
      await serviceMapRealtimeLoader(makeArgs('http://localhost/serviceMap/realtime/blogService')),
    ).toBe(null);
  });

  // 다른 화면 링크를 타고 실려 들어온 application. 그대로 두면 클릭한 노드가 아니라
  // 그 application의 수치를 우측 패널에 보여준다.
  test('drops an application carried into a non-DEFAULT service', async () => {
    const result = (await serviceMapRealtimeLoader(
      makeArgs(`http://localhost/serviceMap/realtime/blogService/${APP}`),
    )) as unknown as Redirect;

    expect(result.__isRedirect).toBe(true);
    expect(result.url).toBe('/serviceMap/realtime/blogService');
  });

  test('returns null when an exception is thrown', async () => {
    jest.spyOn(console, 'error').mockImplementation(() => {});

    expect(await serviceMapRealtimeLoader(makeArgs('not-a-valid-url'))).toBeNull();

    jest.restoreAllMocks();
  });

  // servicemap이 꺼지면 이 화면은 메뉴에서 감춰진다. URL로 직접 들어와도 그리지 않는다.
  test('redirects to the servermap realtime page when serviceMap is disabled', async () => {
    (getConfiguration as jest.Mock).mockResolvedValue({});

    const result = (await serviceMapRealtimeLoader(
      makeArgs(`http://localhost${BASE}`),
    )) as unknown as Redirect;

    expect(result.__isRedirect).toBe(true);
    expect(result.url).toBe(`/serverMap/realtime/${APP}`);
  });
});
