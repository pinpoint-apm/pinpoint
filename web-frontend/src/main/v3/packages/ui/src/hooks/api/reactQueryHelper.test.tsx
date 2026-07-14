import type { Query } from '@tanstack/react-query';

// ErrorToast transitively pulls the ECharts (ESM) stack that babel-jest does not
// transform, so stub it out. These mocks must run before reactQueryHelper (and the
// mocked react-toastify) are imported, so those imports are placed after them.
jest.mock('../../components/Error/ErrorToast', () => ({ ErrorToast: () => null }));
jest.mock('react-toastify', () => ({ toast: { error: jest.fn() } }));
jest.mock('@pinpoint-fe/ui/src/atoms', () => ({
  toastCountAtom: { toString: () => 'toastCountAtom' },
}));

import { toast } from 'react-toastify';
import { handleGlobalQueryError, showGlobalErrorToast } from './reactQueryHelper';

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
