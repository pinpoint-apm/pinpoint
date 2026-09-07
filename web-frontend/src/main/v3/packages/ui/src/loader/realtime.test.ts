import { makeArgs } from './__fixtures__/loaderArgs';
import { realtimeLoader } from './realtime';

jest.mock('react-router', () => ({
  redirect: (url: string) => ({ __isRedirect: true, url }),
}));

// servicemap이 꺼진 상태가 이 화면이 살아 있는 조건이다. 켜져 있으면 메뉴에서 감춰지므로
// 로더가 servicemap 실시간 보기로 옮긴다(맨 아래 테스트).
jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  getConfiguration: jest.fn(() => Promise.resolve({})),
  getRequestService: jest.fn(() => 'DEFAULT'),
}));

import { getConfiguration, getRequestService } from '@pinpoint-fe/ui/src/hooks';

describe('realtimeLoader', () => {
  beforeEach(() => {
    (getConfiguration as jest.Mock).mockResolvedValue({});
    (getRequestService as jest.Mock).mockReturnValue('DEFAULT');
    jest.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    jest.restoreAllMocks();
    jest.clearAllMocks();
  });

  test('returns application when application is present and no query params', async () => {
    const result = await realtimeLoader(
      makeArgs('http://localhost/serverMap/realtime/TestApp@SPRING_BOOT', {
        application: 'TestApp@SPRING_BOOT',
      }),
    );
    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });

  test('redirects to clean realtime URL when query params are present', async () => {
    const result = await realtimeLoader(
      makeArgs('http://localhost/serverMap/realtime/TestApp@SPRING_BOOT?from=2023-11-10-15-00-00', {
        application: 'TestApp@SPRING_BOOT',
      }),
    );
    expect(result).toEqual({
      __isRedirect: true,
      url: '/serverMap/realtime/TestApp@SPRING_BOOT',
    });
  });

  test('returns null when no application is provided', async () => {
    const result = await realtimeLoader(
      makeArgs('http://localhost/serverMap/realtime', { application: '' }),
    );
    expect(result).toBeNull();
  });

  test('returns null when an exception is thrown', async () => {
    const result = await realtimeLoader(
      makeArgs('not-a-valid-url', { application: 'TestApp@SPRING_BOOT' }),
    );
    expect(result).toBeNull();
  });

  // servicemap이 켜지면 이 화면은 메뉴에서 감춰진다. URL로 직접 들어와도 그리지 않는다.
  test('redirects to the serviceMap realtime page when serviceMap is enabled', async () => {
    (getConfiguration as jest.Mock).mockResolvedValue({
      'experimental.enableServiceMap.value': true,
    });

    const result = await realtimeLoader(
      makeArgs('http://localhost/serverMap/realtime/TestApp@SPRING_BOOT', {
        application: 'TestApp@SPRING_BOOT',
      }),
    );

    expect(result).toEqual({
      __isRedirect: true,
      url: '/serviceMap/realtime/DEFAULT/TestApp@SPRING_BOOT',
    });
  });
});
