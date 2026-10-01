import { makeArgs } from './__fixtures__/loaderArgs';
import { threadDumpRouteLoader } from './threadDump';
import { APP_PATH } from '@pinpoint-fe/ui/src/constants';

jest.mock('react-router', () => ({
  // 경로에서 serviceName을 읽을 때 라우트 매칭(`matchRoutes`)을 쓰므로 실제 모듈을 둔다.
  ...jest.requireActual('react-router'),
  redirect: (url: string) => ({ __isRedirect: true, url }),
}));

jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  getConfiguration: jest.fn(() => Promise.resolve({})),
  // enableServiceMap이 꺼진 상태 — serviceName 세그먼트를 붙이지 않는다.
  getRequestService: jest.fn(() => undefined),
}));

import { getConfiguration, getRequestService } from '@pinpoint-fe/ui/src/hooks';

describe('threadDumpRouteLoader', () => {
  beforeEach(() => {
    jest.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    jest.restoreAllMocks();
    jest.clearAllMocks();
    (getRequestService as jest.Mock).mockReturnValue(undefined);
  });

  test('redirects to serverMap when no application is provided', async () => {
    const result = await threadDumpRouteLoader(
      makeArgs('http://localhost/threadDump', { application: '' }),
    );
    expect(result).toEqual({ __isRedirect: true, url: APP_PATH.SERVER_MAP });
  });

  test('redirects to serverMap with application path when agentId is missing', async () => {
    const result = await threadDumpRouteLoader(
      makeArgs('http://localhost/threadDump/TestApp@SPRING_BOOT', {
        application: 'TestApp@SPRING_BOOT',
      }),
    );
    expect(result).toEqual({
      __isRedirect: true,
      url: `${APP_PATH.SERVER_MAP}/TestApp@SPRING_BOOT`,
    });
  });

  test('returns application object when application and agentId are both present', async () => {
    const result = await threadDumpRouteLoader(
      makeArgs('http://localhost/threadDump/TestApp@SPRING_BOOT?agentId=myAgent', {
        application: 'TestApp@SPRING_BOOT',
      }),
    );
    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });

  test('returns null when an exception is thrown', async () => {
    const result = await threadDumpRouteLoader(
      makeArgs('not-a-valid-url', { application: 'TestApp@SPRING_BOOT' }),
    );
    expect(result).toBeNull();
  });

  test('reads the application after the service name segment', async () => {
    const result = await threadDumpRouteLoader(
      makeArgs('http://localhost/threadDump/a%2Fb/TestApp@SPRING_BOOT?agentId=myAgent'),
    );
    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
    expect(getRequestService).not.toHaveBeenCalled();
  });

  test('fills in the current service when the path does not carry one', async () => {
    (getRequestService as jest.Mock).mockReturnValue('svc');
    const result = await threadDumpRouteLoader(
      makeArgs('http://localhost/threadDump/TestApp@SPRING_BOOT?agentId=myAgent'),
    );
    expect(result).toEqual({
      __isRedirect: true,
      url: '/threadDump/svc/TestApp@SPRING_BOOT?agentId=myAgent',
    });
  });

  test('still returns the application when configuration fetch fails', async () => {
    (getConfiguration as jest.Mock).mockRejectedValueOnce(new Error('backend down'));
    const result = await threadDumpRouteLoader(
      makeArgs('http://localhost/threadDump/TestApp@SPRING_BOOT?agentId=myAgent'),
    );
    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });
});
