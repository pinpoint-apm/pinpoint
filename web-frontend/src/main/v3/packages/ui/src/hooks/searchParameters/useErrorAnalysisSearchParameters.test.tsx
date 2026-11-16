import { renderHook } from '@testing-library/react';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useErrorAnalysisSearchParameters } from './useErrorAnalysisSearchParameters';

const mockLocation = { pathname: '', search: '' };
jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useLocation: () => mockLocation,
}));

const renderErrorAnalysisSearchParameters = (search: string) => {
  mockLocation.pathname = '/errorAnalysis/test-app@SPRING_BOOT';
  mockLocation.search = search;
  return renderHook(() => useErrorAnalysisSearchParameters()).result.current;
};

const errorKey = {
  serviceName: 'DEFAULT',
  applicationName: 'test-app',
  agentId: 'agent-1',
  transactionId: 'agent-1^1700000000000^1',
  spanId: '123',
  exceptionId: '1',
};

describe('useErrorAnalysisSearchParameters', () => {
  // The call tree's link to the error analysis (CallTree#getErrorAnalysisHref) encodes
  // transactionInfo once before the query string does, because getSearchParameters decodes twice.
  const callTreeLinkSearch = (transactionInfo: object) =>
    `?${convertParamsToQueryString({
      transactionInfo: encodeURIComponent(JSON.stringify(transactionInfo)),
      openErrorDetail: true,
    })}`;

  test.each([
    ['a percent sign', 'disk 100% used, %d left'],
    ['percent-encoded text', 'path /a%2Fb and %22quoted%22'],
    ['url-like characters', 'a&b=c?d#e+f "quoted" 한글'],
  ])('should read back a call tree link whose message has %s', (_, errorMessage) => {
    const transactionInfo = {
      ...errorKey,
      errorClassName: 'IllegalStateException',
      errorMessage,
    };

    const { parsedTransactionInfo, openErrorDetail } = renderErrorAnalysisSearchParameters(
      callTreeLinkSearch(transactionInfo),
    );

    expect(parsedTransactionInfo).toEqual(transactionInfo);
    expect(openErrorDetail).toBe(true);
  });

  test('should still read a link made before the message was carried', () => {
    // Bookmarks from before #14203 put the JSON in the query string encoded only once.
    const { parsedTransactionInfo } = renderErrorAnalysisSearchParameters(
      `?${convertParamsToQueryString({ transactionInfo: JSON.stringify(errorKey) })}`,
    );

    expect(parsedTransactionInfo).toEqual(errorKey);
  });

  test('should read a malformed transactionInfo as nothing', () => {
    const { parsedTransactionInfo } = renderErrorAnalysisSearchParameters(
      `?${convertParamsToQueryString({ transactionInfo: '{oops' })}`,
    );

    expect(parsedTransactionInfo).toBeUndefined();
  });
});
