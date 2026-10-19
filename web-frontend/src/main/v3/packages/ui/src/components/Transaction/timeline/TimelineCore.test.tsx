import { fireEvent, render, screen } from '@testing-library/react';
import {
  TraceViewerData,
  TransactionInfoType as TransactionInfo,
} from '@pinpoint-fe/ui/src/constants';

// The ui lib barrel also loads the chart helpers, and echarts ships ESM only, which jest cannot
// load. No chart is drawn here.
jest.mock('echarts/core', () => ({ use: jest.fn() }));
jest.mock('echarts/charts', () => ({}));
jest.mock('echarts/components', () => ({}));
jest.mock('echarts/renderers', () => ({}));
jest.mock('echarts/features', () => ({}));

import { TimelineCore } from './TimelineCore';

const traceEvent = {
  cat: 'TOMCAT',
  tid: 1,
  id: '1',
  ts: 1_000,
  ph: 'X',
  dur: 5_000,
  s: '',
  name: 'handleRequest',
  cname: '',
  pid: 'agent-1',
  args: { id: '1', 'Application Name': 'my-app' },
} as unknown as TraceViewerData.TraceEvent;

const transactionInfo = {
  transactionId: 'trace-1',
  callStackStart: 1,
  callStackEnd: 6,
} as TransactionInfo.Response;

// Rendered with no router and no React Query provider, the way an MCP Apps view draws it.
describe('TimelineCore', () => {
  beforeAll(() => {
    global.ResizeObserver = class {
      observe() {}
      unobserve() {}
      disconnect() {}
    };
  });

  test('draws the given trace events without the router or React Query', () => {
    render(
      <TimelineCore
        transactionInfo={transactionInfo}
        traceViewerData={{ traceEvents: [traceEvent] }}
        mapData={[]}
      />,
    );

    // jsdom has no layout, so the flame graph draws no labels; the search runs over the same
    // events, so a hit shows the given data reached the graph.
    const searchBox = screen.getByPlaceholderText('Search trace events...');
    fireEvent.change(searchBox, { target: { value: 'handleRequest' } });
    fireEvent.keyDown(searchBox, { key: 'Enter' });

    expect(screen.getByText(/of\s*1/)).toBeTruthy();
  });
});
