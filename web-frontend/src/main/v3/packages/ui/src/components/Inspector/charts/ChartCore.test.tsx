import { render } from '@testing-library/react';
import { InspectorAgentChart } from '@pinpoint-fe/ui/src/constants';
import type { InspectorChartOptions } from '../../../lib/charts/useChartConfig';

// echarts 자체와 인스턴스 라이프사이클 훅은 모킹해, ChartCore 가 chart.setOption 에 넘기는
// 옵션(순수 변환 로직)만 검증한다. (실제 렌더/canvas 는 이 테스트의 관심사가 아니다)
const mockSetOption = jest.fn();
const mockChartRef = { current: null as HTMLDivElement | null };
const mockChartInstanceRef = { current: { setOption: mockSetOption } };
const mockRenderRef = { current: null as (() => void) | null };

jest.mock('echarts/core', () => ({ use: jest.fn() }));
jest.mock('echarts/charts', () => ({ BarChart: {}, LineChart: {} }));
jest.mock('echarts/components', () => ({
  GridComponent: {},
  TooltipComponent: {},
  LegendComponent: {},
  GraphicComponent: {},
}));
jest.mock('echarts/renderers', () => ({ CanvasRenderer: {} }));
// legend 줄바꿈 행 수 계산은 canvas measureText 에 의존하는데 jsdom 에는 canvas 가 없다.
// grid.bottom 값은 이 테스트의 검증 대상이 아니므로 모듈을 모킹해 jsdom 경고를 피한다.
jest.mock('../../../lib/charts/echartsLegendLayout', () => ({
  getGridBottom: () => 60,
  LEGEND_ICON_WIDTH: 10,
  LEGEND_ITEM_GAP: 8,
}));
jest.mock('../../../lib/charts/useEChartsInstance', () => ({
  useEChartsInstance: () => ({
    chartRef: mockChartRef,
    chartInstanceRef: mockChartInstanceRef,
    renderRef: mockRenderRef,
  }),
}));

// eslint-disable-next-line import/first
import { ChartCore } from './ChartCore';

type MetricValue = {
  fieldName: string;
  unit: string;
  chartType: string;
  valueList: number[];
};

const makeData = (metricValues: MetricValue[], timestamp: number[] = [1000, 2000, 3000]) =>
  ({ title: 'Test', timestamp, metricValues }) as unknown as InspectorAgentChart.Response;

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const renderChart = (data: InspectorAgentChart.Response, chartOptions?: any) => {
  render(
    <ChartCore
      data={data}
      chartOptions={chartOptions as InspectorChartOptions}
      emptyMessage="No Data"
    />,
  );
  const [option, mergeArg] = mockSetOption.mock.calls[0];
  return { option, mergeArg };
};

describe('ChartCore', () => {
  beforeEach(() => {
    mockSetOption.mockClear();
  });

  it('calls setOption with replaceMerge for series and yAxis so stale ones do not linger', () => {
    const { mergeArg } = renderChart(
      makeData([{ fieldName: 'cpu', unit: 'percent', chartType: 'line', valueList: [1, 2, 3] }]),
    );
    expect(mergeArg).toEqual({ replaceMerge: ['series', 'yAxis'] });
  });

  it('converts uncollected (-1) values to null and keeps 0 / positive values', () => {
    const { option } = renderChart(
      makeData([{ fieldName: 'cpu', unit: 'percent', chartType: 'line', valueList: [-1, 0, 5] }]),
    );
    expect(option.series[0].data).toEqual([null, 0, 5]);
  });

  it('draws the y-axis from a 0 baseline with a default max when all values are zero', () => {
    const { option } = renderChart(
      makeData([{ fieldName: 'cpu', unit: 'percent', chartType: 'line', valueList: [0, 0, 0] }]),
    );
    expect(option.yAxis[0].min).toBe(0);
    expect(option.yAxis[0].max).toBe(10); // DEFAULT_CHART_CONFIG.DEFAULT_MAX
  });

  it('lets the y-axis auto-scale (max undefined) when there are positive values', () => {
    const { option } = renderChart(
      makeData([{ fieldName: 'cpu', unit: 'percent', chartType: 'line', valueList: [3, 7, 5] }]),
    );
    expect(option.yAxis[0].min).toBe(0);
    expect(option.yAxis[0].max).toBeUndefined();
  });

  it('draws a value-less secondary axis from 0 (not a centered dashed line)', () => {
    const data = makeData([
      { fieldName: 'heapUsed', unit: 'bytes', chartType: 'line', valueList: [100, 200, 300] },
      { fieldName: 'gcTime', unit: 'ms', chartType: 'line', valueList: [0, 0, 0] },
    ]);
    const chartOptions = {
      seriesOptions: { heapUsed: { unit: 'bytes' }, gcTime: { unit: 'ms' } },
      yAxis: [{ unit: 'bytes' }, { unit: 'ms' }],
      legendShow: true,
    };
    const { option } = renderChart(data, chartOptions);

    const msAxis = option.yAxis[1];
    expect(msAxis.position).toBe('right');
    expect(msAxis.min).toBe(0); // not -10: no symmetric/centered range
    expect(msAxis.max).toBe(10);

    const gcSeries = option.series.find(
      (s: { name: string }) => s.name === 'gcTime',
    );
    expect(gcSeries.data).toEqual([0, 0, 0]); // real 0s drawn at the baseline, not filled/nulled
    expect(gcSeries.lineStyle.type).toBeUndefined(); // not forced dashed
  });

  it('shows the empty-message graphic when there is no collected data', () => {
    const { option } = renderChart(
      makeData([{ fieldName: 'cpu', unit: 'percent', chartType: 'line', valueList: [-1, -1, -1] }]),
    );
    expect(option.graphic).toHaveLength(1);
    expect(option.graphic[0].style.text).toBe('No Data');
  });

  it('renders no empty-message graphic when data exists', () => {
    const { option } = renderChart(
      makeData([{ fieldName: 'cpu', unit: 'percent', chartType: 'line', valueList: [0, 1, 2] }]),
    );
    expect(option.graphic).toEqual([]);
  });

  it('marks a series dashed when seriesOption.dashed is set', () => {
    const chartOptions = {
      seriesOptions: { cpu: { unit: 'percent', dashed: true } },
      yAxis: [{ unit: 'percent' }],
      legendShow: true,
    };
    const { option } = renderChart(
      makeData([{ fieldName: 'cpu', unit: 'percent', chartType: 'line', valueList: [1, 2, 3] }]),
      chartOptions,
    );
    expect(option.series[0].lineStyle.type).toBe('dashed');
  });

  it('excludes tooltip-only fields (chartType: "tooltip") from the drawn series', () => {
    const { option } = renderChart(
      makeData([
        { fieldName: 'cpu', unit: 'percent', chartType: 'line', valueList: [1, 2, 3] },
        { fieldName: 'tip', unit: 'percent', chartType: 'tooltip', valueList: [9, 9, 9] },
      ]),
    );
    expect(option.series).toHaveLength(1);
    expect(option.series[0].name).toBe('cpu');
  });

  it('maps a bar chart type to an echarts bar series', () => {
    const chartOptions = {
      seriesOptions: { cpu: { unit: 'percent', type: 'bar' } },
      yAxis: [{ unit: 'percent' }],
      legendShow: true,
    };
    const { option } = renderChart(
      makeData([{ fieldName: 'cpu', unit: 'percent', chartType: 'bar', valueList: [1, 2, 3] }]),
      chartOptions,
    );
    expect(option.series[0].type).toBe('bar');
  });

  it('hides legend names when legendShow is false', () => {
    const chartOptions = {
      seriesOptions: { cpu: { unit: 'percent' } },
      yAxis: [{ unit: 'percent' }],
      legendShow: false,
    };
    const { option } = renderChart(
      makeData([{ fieldName: 'cpu', unit: 'percent', chartType: 'line', valueList: [1, 2, 3] }]),
      chartOptions,
    );
    expect(option.legend.data).toEqual([]);
    expect(option.series).toHaveLength(1); // series still drawn
  });

  it('focuses the hovered series so the others dim on hover (like SystemMetric charts)', () => {
    const { option } = renderChart(
      makeData([{ fieldName: 'cpu', unit: 'percent', chartType: 'line', valueList: [1, 2, 3] }]),
    );
    expect(option.series[0].emphasis.focus).toBe('series');
  });
});
