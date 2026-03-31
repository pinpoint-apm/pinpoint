import { threadDumpRouteLoader } from './threadDump';
import { APP_PATH } from '@pinpoint-fe/ui/src/constants';

jest.mock('react-router-dom', () => ({
  redirect: (url: string) => ({ __isRedirect: true, url }),
}));

const makeArgs = (url: string, params: Record<string, string> = {}) => ({
  params,
  request: { url } as Request,
  context: {},
});

describe('threadDumpRouteLoader', () => {
  beforeEach(() => {
    jest.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  test('redirects to serverMap when no application is provided', () => {
    const result = threadDumpRouteLoader(makeArgs('http://localhost/threadDump', { application: '' }));
    expect(result).toEqual({ __isRedirect: true, url: APP_PATH.SERVER_MAP });
  });

  test('redirects to serverMap with application path when agentId is missing', () => {
    const result = threadDumpRouteLoader(
      makeArgs('http://localhost/threadDump/TestApp@SPRING_BOOT', {
        application: 'TestApp@SPRING_BOOT',
      }),
    );
    expect(result).toEqual({
      __isRedirect: true,
      url: `${APP_PATH.SERVER_MAP}/TestApp@SPRING_BOOT`,
    });
  });

  test('returns application object when application and agentId are both present', () => {
    const result = threadDumpRouteLoader(
      makeArgs('http://localhost/threadDump/TestApp@SPRING_BOOT?agentId=myAgent', {
        application: 'TestApp@SPRING_BOOT',
      }),
    );
    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });

  test('returns null when an exception is thrown', () => {
    const result = threadDumpRouteLoader({
      params: { application: 'TestApp@SPRING_BOOT' },
      request: { url: 'not-a-valid-url' } as unknown as Request,
      context: {},
    });
    expect(result).toBeNull();
  });
});
