import { renderHook } from '@testing-library/react';
import { useTransactionSearchParameters } from './useTransactionSearchParameters';

const mockLocation = { pathname: '', search: '' };
jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useLocation: () => mockLocation,
}));

const renderTransactionSearchParameters = (pathname: string, search = '') => {
  mockLocation.pathname = pathname;
  mockLocation.search = search;
  return renderHook(() => useTransactionSearchParameters()).result.current;
};

describe('useTransactionSearchParameters', () => {
  test('splits the service name off a transactionList path that carries one', () => {
    const { application, serviceName } = renderTransactionSearchParameters(
      '/transactionList/my-service/test-app@SPRING_BOOT',
    );

    expect(serviceName).toBe('my-service');
    expect(application).toEqual({ applicationName: 'test-app', serviceType: 'SPRING_BOOT' });
  });

  test('keeps a legacy transactionList path resolving without a service name', () => {
    const { application, serviceName } = renderTransactionSearchParameters(
      '/transactionList/test-app@SPRING_BOOT',
    );

    expect(serviceName).toBeUndefined();
    expect(application).toEqual({ applicationName: 'test-app', serviceType: 'SPRING_BOOT' });
  });

  test('splits the service name off a transactionDetail path too', () => {
    // transactionList의 외부 링크로 새 탭에서 열리므로 transactionDetail도 serviceName을 싣는다.
    const { application, serviceName } = renderTransactionSearchParameters(
      '/transactionDetail/my-service/test-app@SPRING_BOOT',
    );

    expect(serviceName).toBe('my-service');
    expect(application).toEqual({ applicationName: 'test-app', serviceType: 'SPRING_BOOT' });
  });

  test('keeps a legacy transactionDetail path resolving without a service name', () => {
    const { application, serviceName } = renderTransactionSearchParameters(
      '/transactionDetail/test-app@SPRING_BOOT',
    );

    expect(serviceName).toBeUndefined();
    expect(application).toEqual({ applicationName: 'test-app', serviceType: 'SPRING_BOOT' });
  });

  test('does not split a service name off paths that never carry one', () => {
    // serviceName 분리는 `SERVICE_NAME_IN_PATH_PAGES`에 등록된 경로에서만 해야 한다. 그 외에는
    // '@'가 든 applicationName이 service/application으로 쪼개지면 안 된다.
    const { application, serviceName } = renderTransactionSearchParameters(
      '/serverMap/test@app@SPRING_BOOT',
    );

    expect(serviceName).toBeUndefined();
    expect(application).toEqual({ applicationName: 'test@app', serviceType: 'SPRING_BOOT' });
  });

  test('decodes an encoded service name from the path', () => {
    const { application, serviceName } = renderTransactionSearchParameters(
      '/transactionList/a%2Fb/test-app@SPRING_BOOT',
    );

    expect(serviceName).toBe('a/b');
    expect(application).toEqual({ applicationName: 'test-app', serviceType: 'SPRING_BOOT' });
  });

  test('returns no application when the path has no application segment', () => {
    const { application, serviceName } = renderTransactionSearchParameters('/transactionList');

    expect(application).toBeNull();
    expect(serviceName).toBeUndefined();
  });
});
