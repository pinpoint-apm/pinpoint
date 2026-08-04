import { renderHook } from '@testing-library/react';
import { useTransactionSearchParameters } from './useTransactionSearchParameters';

const mockLocation = { pathname: '', search: '' };
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
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
      '/transactionList/my-service@test-app@SPRING_BOOT',
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

  test('does not split a service name off paths that never carry one', () => {
    // transactionDetail은 serviceName을 싣지 않으므로, '@'가 든 applicationName이
    // service/application으로 쪼개지면 안 된다.
    const { application, serviceName } = renderTransactionSearchParameters(
      '/transactionDetail/test@app@SPRING_BOOT',
    );

    expect(serviceName).toBeUndefined();
    expect(application).toEqual({ applicationName: 'test@app', serviceType: 'SPRING_BOOT' });
  });

  test('returns no application when the path has no application segment', () => {
    const { application, serviceName } = renderTransactionSearchParameters('/transactionList');

    expect(application).toBeNull();
    expect(serviceName).toBeUndefined();
  });
});
