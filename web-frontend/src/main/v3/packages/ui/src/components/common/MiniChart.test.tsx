import { render } from '@testing-library/react';
import { Chart, MetricValue, MetricValueGroup } from '@pinpoint-fe/ui/src/constants';
import { CHART_SERIES_COLORS } from '../../lib/colors';

// echarts 자체와 인스턴스 라이프사이클 훅은 모킹해, MiniChart 가 chart.setOption 에 넘기는
// 옵션(순수 변환 로직)만 검증한다. (실제 렌더/canvas 는 이 테스트의 관심사가 아니다)
const mockSetOption = jest.fn();
const mockChartRef = { current: null as HTMLDivElement | null };
const mockChartInstanceRef = { current: { setOption: mockSetOption } };

jest.mock('echarts/core', () => ({ use: jest.fn() }));
jest.mock('echarts/charts', () => ({ BarChart: {}, LineChart: {} }));
jest.mock('echarts/components', () => ({ GridComponent: {}, MarkLineComponent: {} }));
jest.mock('echarts/renderers', () => ({ CanvasRenderer: {} }));
jest.mock('../../lib/charts/useEChartsInstance', () => ({
  useEChartsInstance: () => ({
    chartRef: mockChartRef,
    chartInstanceRef: mockChartInstanceRef,
    renderRef: { current: null },
  }),
}));

// eslint-disable-next-line import/first
import { MiniChart } from './MiniChart';

const makeMetricValue = (fieldName: string, values: number[]): MetricValue => ({
  fieldName,
  values,
});

const makeGroup = (chartType: string, metricValues: MetricValue[]): MetricValueGroup => ({
  groupName: 'group',
  chartType,
  unit: '',
  metricValues,
});

const makeChart = (
  metricValueGroups: MetricValueGroup[],
  timestamp: number[] = [1000, 2000, 3000],
): Chart => ({ title: 'Test', timestamp, metricValueGroups });

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const renderChart = (chart: Chart) => {
  render(<MiniChart chart={chart} />);
  const [option, mergeArg] = mockSetOption.mock.calls[0];
  return { option, mergeArg };
};

describe('MiniChart', () => {
  beforeEach(() => {
    mockSetOption.mockClear();
  });

  it('calls setOption with notMerge so stale series do not linger', () => {
    const { mergeArg } = renderChart(
      makeChart([makeGroup('line', [makeMetricValue('cpu', [1, 2, 3])])]),
    );
    expect(mergeArg).toEqual({ notMerge: true });
  });

  it('disables animation to match the other echarts charts (per-row render cost)', () => {
    const { option } = renderChart(
      makeChart([makeGroup('line', [makeMetricValue('cpu', [1, 2, 3])])]),
    );
    expect(option.animation).toBe(false);
  });

  it('builds [timestamp, value] points and converts -1/null to null (keeping 0/positive)', () => {
    const { option } = renderChart(
      makeChart([makeGroup('line', [makeMetricValue('cpu', [-1, 0, 5])])]),
    );
    expect(option.series[0].data).toEqual([
      [1000, null],
      [2000, 0],
      [3000, 5],
    ]);
  });

  it('attaches the max-value markLine only to the first series', () => {
    const { option } = renderChart(
      makeChart([
        makeGroup('line', [makeMetricValue('cpu', [1, 2, 3]), makeMetricValue('mem', [4, 5, 6])]),
      ]),
    );
    expect(option.series[0].markLine).toBeDefined();
    expect(option.series[1].markLine).toBeUndefined();
  });

  it('uses the overall max for the markLine and rounds its label to 2 decimals', () => {
    const { option } = renderChart(
      makeChart([makeGroup('line', [makeMetricValue('cpu', [0.1, 1.23456, 0.2])])]),
    );
    expect(option.series[0].markLine.data).toEqual([{ yAxis: 1.23456 }]);
    expect(option.series[0].markLine.label.formatter()).toBe('1.23');
  });

  it('omits the markLine when every value is uncollected (-1)', () => {
    const { option } = renderChart(
      makeChart([makeGroup('line', [makeMetricValue('cpu', [-1, -1, -1])])]),
    );
    expect(option.series[0].markLine).toBeUndefined();
  });

  it('maps a bar chartType to a bar series without smoothing or area fill', () => {
    const { option } = renderChart(
      makeChart([makeGroup('bar', [makeMetricValue('cpu', [1, 2, 3])])]),
    );
    expect(option.series[0].type).toBe('bar');
    expect(option.series[0].smooth).toBe(false);
    expect(option.series[0].areaStyle).toBeUndefined();
  });

  it('maps an area chartType to a smoothed line series with an area fill', () => {
    const { option } = renderChart(
      makeChart([makeGroup('area', [makeMetricValue('cpu', [1, 2, 3])])]),
    );
    expect(option.series[0].type).toBe('line');
    expect(option.series[0].smooth).toBe(true);
    expect(option.series[0].areaStyle).toEqual({ opacity: 0.4 });
  });

  it('assigns palette colors by the metricValue index within its group', () => {
    const { option } = renderChart(
      makeChart([
        makeGroup('line', [makeMetricValue('cpu', [1, 2, 3]), makeMetricValue('mem', [4, 5, 6])]),
      ]),
    );
    expect(option.series[0].itemStyle.color).toBe(CHART_SERIES_COLORS[0]);
    expect(option.series[1].itemStyle.color).toBe(CHART_SERIES_COLORS[1]);
  });

  it('renders an empty series list without crashing when there is no data', () => {
    const { option } = renderChart(makeChart([], []));
    expect(option.series).toEqual([]);
  });
});
