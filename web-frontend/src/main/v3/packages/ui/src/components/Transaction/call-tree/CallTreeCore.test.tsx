import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { TransactionInfoType as TransactionInfo } from '@pinpoint-fe/ui/src/constants';

// The ui lib barrel also loads the chart helpers, and echarts ships ESM only, which jest cannot
// load. No chart is drawn here.
jest.mock('echarts/core', () => ({ use: jest.fn() }));
jest.mock('echarts/charts', () => ({}));
jest.mock('echarts/components', () => ({}));
jest.mock('echarts/renderers', () => ({}));
jest.mock('echarts/features', () => ({}));

// The code viewers reach the components barrel (through the copy button's toast), which loads
// packages jest cannot parse. The SQL detail they draw is not under test.
jest.mock('../../CollapsibleCodeViewer', () => ({ CollapsibleCodeViewer: () => null }));
jest.mock('../../HighLightCode', () => ({ HighLightCode: () => null }));

// jsdom has no layout, so the virtualizer would measure a 0px viewport and draw no rows.
jest.mock('@tanstack/react-virtual', () => ({
  useVirtualizer: ({ count }: { count: number }) => ({
    getVirtualItems: () =>
      Array.from({ length: count }, (_, index) => ({ index, key: index, start: index, size: 1 })),
    getTotalSize: () => count,
    measureElement: () => {},
    scrollToIndex: () => {},
  }),
}));

import { CallTreeCore } from './CallTreeCore';

const row = (fields: Partial<TransactionInfo.CallStackKeyValueMap>) =>
  ({
    tab: 0,
    arguments: '',
    isMethod: false,
    hasException: false,
    ...fields,
  }) as TransactionInfo.CallStackKeyValueMap;

const methodRow = row({ id: 1, title: 'doWork', isMethod: true });
const exceptionRow = row({
  id: 2,
  title: 'failingCall',
  isMethod: true,
  hasException: true,
  errorKey: { exceptionId: 'exception-1' },
});
const otelLinkRow = row({
  id: 3,
  title: 'Link',
  arguments: JSON.stringify({
    traceId: 'trace-1',
    spanId: '10',
    linkTraceId: 'trace-2',
    linkSpanId: '20',
  }),
});
const sqlRow = row({ id: 4, title: 'SQL', arguments: 'select * from t where id = ?' });
const bindValueRow = row({ id: 5, title: 'SQL-BindValue', arguments: '42' });

const rows = [methodRow, exceptionRow, otelLinkRow, sqlRow];
const mapData = [...rows, bindValueRow];
const metaData = {
  callStackStart: 1700000000000,
  callTreeTimelineDurationNanos: 1_000_000,
} as TransactionInfo.Response;

// Rendered with no router and no React Query provider, the way an MCP Apps view draws it.
describe('CallTreeCore', () => {
  beforeAll(() => {
    // jsdom has no ResizeObserver; the table only uses it to re-measure on resize.
    global.ResizeObserver = class {
      observe() {}
      unobserve() {}
      disconnect() {}
    };
  });

  test('draws the rows without the router or React Query', () => {
    render(<CallTreeCore data={rows} mapData={mapData} metaData={metaData} />);

    expect(screen.getByText('doWork')).toBeTruthy();
    // Without link builders, the rows that would link show their titles only.
    expect(screen.getByText('failingCall').closest('a')).toBeNull();
    expect(screen.getByText('Link')).toBeTruthy();
  });

  test('links the rows through the given builders', () => {
    render(
      <CallTreeCore
        data={rows}
        mapData={mapData}
        metaData={metaData}
        getErrorAnalysisHref={(errorKey) => `/errorAnalysis?exceptionId=${errorKey.exceptionId}`}
        renderOtelLink={(otelLink, text) => <span>{`${text} to ${otelLink.linkTraceId}`}</span>}
      />,
    );

    expect(screen.getByText('failingCall').closest('a')?.getAttribute('href')).toBe(
      '/errorAnalysis?exceptionId=exception-1',
    );
    expect(screen.getByText('Link to trace-2')).toBeTruthy();
  });

  test('binds the SQL through the given function', async () => {
    const bindSql = jest.fn().mockResolvedValue({ bindedQuery: 'select * from t where id = 42' });
    render(<CallTreeCore data={rows} mapData={mapData} metaData={metaData} bindSql={bindSql} />);

    fireEvent.click(screen.getByRole('button', { name: /SQL/ }));

    await waitFor(() => expect(bindSql).toHaveBeenCalledTimes(1));
    const formData: FormData = bindSql.mock.calls[0][0];
    expect(formData.get('type')).toBe('sql');
    expect(formData.get('metaData')).toBe('select * from t where id = ?');
    expect(formData.get('bind')).toBe('42');
  });
});
