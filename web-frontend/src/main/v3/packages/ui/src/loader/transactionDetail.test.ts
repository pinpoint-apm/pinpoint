import { transactionDetailRouteLoader } from './transactionDetail';
import { APP_PATH } from '@pinpoint-fe/ui/src/constants';

jest.mock('react-router-dom', () => ({
  redirect: (url: string) => ({ __isRedirect: true, url }),
}));

const makeArgs = (url: string, params: Record<string, string> = {}) => ({
  params,
  request: { url } as Request,
  context: {},
});

describe('transactionDetailRouteLoader', () => {
  beforeEach(() => {
    jest.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  test('returns null when no application param is provided', () => {
    const result = transactionDetailRouteLoader(
      makeArgs('http://localhost/transactionDetail', { application: '' }),
    );
    expect(result).toBeNull();
  });

  test('redirects to serverMap when application is present but query params are empty', () => {
    const result = transactionDetailRouteLoader(
      makeArgs('http://localhost/transactionDetail/TestApp@SPRING_BOOT', {
        application: 'TestApp@SPRING_BOOT',
      }),
    );
    expect(result).toEqual({ __isRedirect: true, url: APP_PATH.SERVER_MAP });
  });

  test('redirects to serverMap when transactionInfo param is missing', () => {
    const result = transactionDetailRouteLoader(
      makeArgs('http://localhost/transactionDetail/TestApp@SPRING_BOOT?agentId=myAgent', {
        application: 'TestApp@SPRING_BOOT',
      }),
    );
    expect(result).toEqual({ __isRedirect: true, url: APP_PATH.SERVER_MAP });
  });

  test('returns application object when transactionInfo param is present', () => {
    const result = transactionDetailRouteLoader(
      makeArgs(
        'http://localhost/transactionDetail/TestApp@SPRING_BOOT?transactionInfo=abc123',
        { application: 'TestApp@SPRING_BOOT' },
      ),
    );
    expect(result).toEqual({ applicationName: 'TestApp', serviceType: 'SPRING_BOOT' });
  });

  test('returns null when an exception is thrown', () => {
    const result = transactionDetailRouteLoader({
      params: { application: 'TestApp@SPRING_BOOT' },
      request: { url: 'not-a-valid-url' } as unknown as Request,
      context: {},
    });
    expect(result).toBeNull();
  });
});
