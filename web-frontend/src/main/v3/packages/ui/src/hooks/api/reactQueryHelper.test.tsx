import type { Query } from '@tanstack/react-query';

// ErrorToast transitively pulls the ECharts (ESM) stack that babel-jest does not
// transform, so stub it out. These mocks must run before reactQueryHelper (and the
// mocked react-toastify) are imported, so those imports are placed after them.
jest.mock('../../components/Error/ErrorToast', () => ({ ErrorToast: () => null }));
jest.mock('react-toastify', () => ({ toast: { error: jest.fn() } }));
jest.mock('@pinpoint-fe/ui/src/atoms', () => {
  // serviceNameFetchInterceptor가 읽는 atom들도 함께 제공해야 한다(실제 atom이어야 store 읽기가 동작).
  const { atom } = require('jotai');
  return {
    toastCountAtom: atom(0),
    selectedServiceAtom: atom('DEFAULT'),
    DEFAULT_SERVICE: 'DEFAULT',
  };
});

import { toast } from 'react-toastify';
import { getDefaultStore } from 'jotai';
import { QueryClient } from '@tanstack/react-query';
import { selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import {
  handleGlobalQueryError,
  serviceScopedQueryKeyHashFn,
  showGlobalErrorToast,
} from './reactQueryHelper';

const makeQuery = (over: Partial<Query> = {}) =>
  ({ queryHash: '["/api/test",""]', meta: undefined, ...over }) as unknown as Query;

describe('reactQueryHelper global query error handling', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('shows a global error toast for a failed query', () => {
    handleGlobalQueryError(new Error('boom'), makeQuery({ queryHash: 'hash-1' }));

    expect(toast.error).toHaveBeenCalledTimes(1);
    const options = (toast.error as jest.Mock).mock.calls[0][1];
    // toastId is keyed on the query hash so repeated polling failures dedupe instead of stacking.
    expect(options.toastId).toBe('hash-1');
    expect(options.autoClose).toBe(false);
  });

  test('does not toast when the query opts out via meta.ignoreGlobalError', () => {
    handleGlobalQueryError(new Error('boom'), makeQuery({ meta: { ignoreGlobalError: true } }));

    expect(toast.error).not.toHaveBeenCalled();
  });

  test('still toasts when meta is present without the ignore flag', () => {
    handleGlobalQueryError(new Error('boom'), makeQuery({ meta: { ignoreGlobalError: false } }));

    expect(toast.error).toHaveBeenCalledTimes(1);
  });

  test('showGlobalErrorToast forwards the toastId option', () => {
    showGlobalErrorToast(new Error('x'), { toastId: 'abc' });

    expect((toast.error as jest.Mock).mock.calls[0][1].toastId).toBe('abc');
  });
});

describe('serviceScopedQueryKeyHashFn', () => {
  const store = getDefaultStore();

  beforeEach(() => {
    store.set(selectedServiceAtom, 'DEFAULT');
    window.history.replaceState({}, '', '/serviceMap');
  });

  test('hashes the same queryKey differently per requested service', () => {
    store.set(selectedServiceAtom, 'service-a');
    const hashOfA = serviceScopedQueryKeyHashFn(['/api/agents/search-application']);

    store.set(selectedServiceAtom, 'service-b');
    const hashOfB = serviceScopedQueryKeyHashFn(['/api/agents/search-application']);

    expect(hashOfA).not.toBe(hashOfB);
  });

  test('hashes the servermap path the same as any other page', () => {
    // ServerMap도 service 예외가 아니므로, 같은 service라면 페이지가 달라도 같은 캐시를 쓴다.
    store.set(selectedServiceAtom, 'service-a');
    const hashOnServiceMap = serviceScopedQueryKeyHashFn(['/api/agents/search-application']);

    window.history.replaceState({}, '', '/serverMap/app-name@TOMCAT');

    expect(serviceScopedQueryKeyHashFn(['/api/agents/search-application'])).toBe(hashOnServiceMap);
  });

  test.each([END_POINTS.CONFIGURATION, END_POINTS.SERVICES, END_POINTS.SERVER_TIME])(
    'does not scope %s by service',
    (endPoint) => {
      // configuration이 service별로 나뉘면 service를 바꿀 때마다 다시 로딩되고,
      // 그동안 InitialFetchOutlet이 자식을 렌더하지 않아 화면이 사라진다.
      store.set(selectedServiceAtom, 'service-a');
      const hashOnServiceA = serviceScopedQueryKeyHashFn([endPoint]);

      store.set(selectedServiceAtom, 'service-b');

      expect(serviceScopedQueryKeyHashFn([endPoint])).toBe(hashOnServiceA);
    },
  );

  test('keeps each service cache separate for one queryKey', () => {
    const client = new QueryClient({
      defaultOptions: { queries: { queryKeyHashFn: serviceScopedQueryKeyHashFn } },
    });
    const queryKey = ['/api/agents/search-application'];

    store.set(selectedServiceAtom, 'service-a');
    client.setQueryData(queryKey, 'service-a-data');

    // 같은 queryKey지만 다른 service에서는 캐시가 비어 있어야 한다.
    store.set(selectedServiceAtom, 'service-b');
    expect(client.getQueryData(queryKey)).toBeUndefined();

    client.setQueryData(queryKey, 'service-b-data');
    expect(client.getQueryData(queryKey)).toBe('service-b-data');

    store.set(selectedServiceAtom, 'service-a');
    expect(client.getQueryData(queryKey)).toBe('service-a-data');
  });
});
