import { realtimeLoader } from './realtime';

jest.mock('react-router-dom', () => ({
  redirect: (url: string) => ({ __isRedirect: true, url }),
}));

const makeArgs = (url: string, params: Record<string, string> = {}) => ({
  params,
  request: { url } as Request,
  context: {},
});

describe('realtimeLoader', () => {
  beforeEach(() => {
    jest.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  test('returns application when application is present and no query params', () => {
    const result = realtimeLoader(
      makeArgs('http://localhost/serverMap/realtime/TestApp@SPRING_BOOT', {
        application: 'TestApp@SPRING_BOOT',
      }),
    );
    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });

  test('redirects to clean realtime URL when query params are present', () => {
    const result = realtimeLoader(
      makeArgs(
        'http://localhost/serverMap/realtime/TestApp@SPRING_BOOT?from=2023-11-10-15-00-00',
        { application: 'TestApp@SPRING_BOOT' },
      ),
    );
    expect(result).toEqual({
      __isRedirect: true,
      url: '/serverMap/realtime/TestApp@SPRING_BOOT',
    });
  });

  test('returns null when no application is provided', () => {
    const result = realtimeLoader(
      makeArgs('http://localhost/serverMap/realtime', { application: '' }),
    );
    expect(result).toBeNull();
  });

  test('returns null when an exception is thrown', () => {
    const result = realtimeLoader({
      params: { application: 'TestApp@SPRING_BOOT' },
      request: { url: 'not-a-valid-url' } as unknown as Request,
      context: {},
    });
    expect(result).toBeNull();
  });
});
