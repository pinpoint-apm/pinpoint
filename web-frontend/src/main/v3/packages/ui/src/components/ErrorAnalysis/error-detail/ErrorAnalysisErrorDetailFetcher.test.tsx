import React from 'react';
import { render, screen } from '@testing-library/react';
import { ErrorAnalysisTransactionInfo } from '@pinpoint-fe/ui/src/constants';

// The utils barrel also loads the chart helpers, and echarts ships ESM only, which jest cannot
// load. No chart is drawn here.
jest.mock('echarts/core', () => ({ use: jest.fn() }));
jest.mock('echarts/charts', () => ({}));
jest.mock('echarts/components', () => ({}));
jest.mock('echarts/renderers', () => ({}));
jest.mock('echarts/features', () => ({}));

const mockData: { current: ErrorAnalysisTransactionInfo.ErrorData[] } = { current: [] };

jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  useGetErrorAnalysisTransactionInfoData: () => ({ data: mockData.current }),
  useRequestService: () => 'DEFAULT',
  useTimezone: () => ['UTC'],
}));
// The code viewer and the copy button reach packages jest cannot parse; they are not under test.
jest.mock('../..', () => ({ CollapsibleCodeViewer: () => null }));
jest.mock('../../Button', () => ({
  ClipboardCopyButton: ({ children }: { children: React.ReactNode }) => <span>{children}</span>,
}));

import { ErrorAnalysisErrorDetailFetcher } from './ErrorAnalysisErrorDetailFetcher';

const card = (exceptionDepth: number, errorClassName: string, errorMessage: string) =>
  ({
    exceptionDepth,
    errorClassName,
    errorMessage,
    timestamp: 0,
    stackTrace: [],
  }) as unknown as ErrorAnalysisTransactionInfo.ErrorData;

const errorInfo = {
  applicationName: 'app',
  agentId: 'agent',
  transactionId: 'tx',
  spanId: '1',
  exceptionId: '2',
};

const scrolled = () =>
  (Element.prototype.scrollIntoView as jest.Mock).mock.contexts.map((el: Element) =>
    el.textContent?.includes('IllegalArgumentException')
      ? 'IllegalArgumentException'
      : 'NestedServletException',
  );

describe('ErrorAnalysisErrorDetailFetcher', () => {
  beforeEach(() => {
    // jsdom has no layout, so it does not implement scrolling.
    Element.prototype.scrollIntoView = jest.fn();
    mockData.current = [
      card(0, 'org.springframework.web.util.NestedServletException', 'Request processing failed'),
      card(1, 'java.lang.IllegalArgumentException', 'bad id'),
    ];
  });

  test('should start at the top when nothing was clicked', () => {
    render(<ErrorAnalysisErrorDetailFetcher errorInfo={errorInfo} />);

    expect(Element.prototype.scrollIntoView).not.toHaveBeenCalled();
    expect(document.querySelector('[aria-current]')).toBeNull();
  });

  test('should scroll to and mark the clicked cause', () => {
    render(
      <ErrorAnalysisErrorDetailFetcher
        errorInfo={errorInfo}
        focus={{ errorClassName: 'IllegalArgumentException', errorMessage: 'bad id' }}
      />,
    );

    expect(scrolled()).toEqual(['IllegalArgumentException']);
    expect(document.querySelector('[aria-current="true"]')?.textContent).toContain(
      'java.lang.IllegalArgumentException',
    );
  });

  test('should not scroll again when the same chain is fetched again', () => {
    const focus = { exceptionDepth: 1 };
    const { rerender } = render(
      <ErrorAnalysisErrorDetailFetcher errorInfo={errorInfo} focus={focus} />,
    );
    mockData.current = [...mockData.current];
    rerender(
      <ErrorAnalysisErrorDetailFetcher errorInfo={errorInfo} focus={{ exceptionDepth: 1 }} />,
    );

    expect(Element.prototype.scrollIntoView).toHaveBeenCalledTimes(1);
  });

  test('should scroll again when another cause is clicked', () => {
    const { rerender } = render(
      <ErrorAnalysisErrorDetailFetcher errorInfo={errorInfo} focus={{ exceptionDepth: 1 }} />,
    );
    rerender(
      <ErrorAnalysisErrorDetailFetcher errorInfo={errorInfo} focus={{ exceptionDepth: 0 }} />,
    );

    expect(scrolled()).toEqual(['IllegalArgumentException', 'NestedServletException']);
  });

  test('should start at the top when the clicked exception is not in the chain', () => {
    render(
      <ErrorAnalysisErrorDetailFetcher errorInfo={errorInfo} focus={{ errorClassName: 'ERROR' }} />,
    );

    expect(Element.prototype.scrollIntoView).not.toHaveBeenCalled();
  });
});
