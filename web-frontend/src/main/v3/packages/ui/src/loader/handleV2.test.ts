import { handleV2RouteLoader } from './handleV2';
import { APP_PATH } from '@pinpoint-fe/ui/src/constants';

jest.mock('react-router-dom', () => ({
  redirect: (url: string) => ({ __isRedirect: true, url }),
}));

const makeArgs = (url: string, params: Record<string, string> = {}) => ({
  params,
  request: { url } as Request,
  context: {},
});

describe('handleV2RouteLoader', () => {
  test('redirects to serverMap with formatted v3 date params for valid v2 params', () => {
    const result = handleV2RouteLoader(
      makeArgs('http://localhost/main/TestApp@SPRING_BOOT/30m/2023-11-10-15-00-00', {
        application: 'TestApp@SPRING_BOOT',
        period: '30m',
        endTime: '2023-11-10-15-00-00',
      }),
    ) as { __isRedirect: boolean; url: string };

    expect(result.__isRedirect).toBe(true);
    expect(result.url).toContain(`${APP_PATH.SERVER_MAP}/TestApp@SPRING_BOOT`);
    expect(result.url).toContain('to=2023-11-10-15-00-00');
    expect(result.url).toContain('from=2023-11-10-14-30-00');
  });

  test('includes extra v2 query params in the redirect URL', () => {
    const result = handleV2RouteLoader(
      makeArgs(
        'http://localhost/main/TestApp@SPRING_BOOT/30m/2023-11-10-15-00-00?inbound=1&outbound=2',
        { application: 'TestApp@SPRING_BOOT', period: '30m', endTime: '2023-11-10-15-00-00' },
      ),
    ) as { url: string };

    expect(result.url).toContain('inbound=1');
    expect(result.url).toContain('outbound=2');
  });

  test('falls back to base serverMap path when period is invalid', () => {
    const result = handleV2RouteLoader(
      makeArgs('http://localhost/main/TestApp@SPRING_BOOT/bad-period/2023-11-10-15-00-00', {
        application: 'TestApp@SPRING_BOOT',
        period: 'bad-period',
        endTime: '2023-11-10-15-00-00',
      }),
    );
    expect(result).toEqual({
      __isRedirect: true,
      url: `${APP_PATH.SERVER_MAP}/TestApp@SPRING_BOOT`,
    });
  });

  test('falls back to base serverMap path when endTime is invalid', () => {
    const result = handleV2RouteLoader(
      makeArgs('http://localhost/main/TestApp@SPRING_BOOT/30m/not-a-date', {
        application: 'TestApp@SPRING_BOOT',
        period: '30m',
        endTime: 'not-a-date',
      }),
    );
    expect(result).toEqual({
      __isRedirect: true,
      url: `${APP_PATH.SERVER_MAP}/TestApp@SPRING_BOOT`,
    });
  });
});
