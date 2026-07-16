import { render } from '@testing-library/react';
import { colors } from '@pinpoint-fe/ui/src/constants';

// echarts 자체와 인스턴스 라이프사이클 훅은 모킹해, AgentStatisticChart 가 chart.setOption 에
// 넘기는 옵션(순수 변환 로직)만 검증한다. (실제 렌더/canvas 는 이 테스트의 관심사가 아니다)
const mockSetOption = jest.fn();
const mockChartRef = { current: null as HTMLDivElement | null };
const mockChartInstanceRef = { current: { setOption: mockSetOption } };

jest.mock('echarts/core', () => ({ use: jest.fn() }));
jest.mock('echarts/charts', () => ({ BarChart: {} }));
jest.mock('echarts/components', () => ({ GridComponent: {}, LegendComponent: {} }));
jest.mock('echarts/renderers', () => ({ CanvasRenderer: {} }));
jest.mock('../../../lib/charts/useEChartsInstance', () => ({
  useEChartsInstance: () => ({
    chartRef: mockChartRef,
    chartInstanceRef: mockChartInstanceRef,
    renderRef: { current: null },
  }),
}));

// eslint-disable-next-line import/first
import { AgentStatisticChart, ChartData } from './AgentStatisticChart';

const renderChart = (type: 'vmVersion' | 'agentVersion', chartData?: ChartData[]) => {
  render(<AgentStatisticChart type={type} chartData={chartData} />);
  const [option, mergeArg] = mockSetOption.mock.calls[0];
  return { option, mergeArg };
};

describe('AgentStatisticChart', () => {
  beforeEach(() => {
    mockSetOption.mockClear();
  });

  it('calls setOption with replaceMerge so stale series/axes do not linger', () => {
    const { mergeArg } = renderChart('vmVersion', [{ vmVersion: '1.8', value: 3 }]);
    expect(mergeArg).toEqual({ replaceMerge: ['series', 'yAxis'] });
  });

  it('disables animation to match the other echarts charts', () => {
    const { option } = renderChart('vmVersion', [{ vmVersion: '1.8', value: 3 }]);
    expect(option.animation).toBe(false);
  });

  it('renders a horizontal bar: value on the x axis and version categories on the y axis', () => {
    const { option } = renderChart('vmVersion', [
      { vmVersion: '1.8', value: 3 },
      { vmVersion: '11', value: 5 },
    ]);
    expect(option.xAxis.type).toBe('value');
    expect(option.yAxis.type).toBe('category');
    expect(option.yAxis.data).toEqual(['1.8', '11']);
    expect(option.series[0].data).toEqual([3, 5]);
    expect(option.series[0].type).toBe('bar');
  });

  it('inverts the y axis so the first category renders at the top (recharts parity)', () => {
    const { option } = renderChart('vmVersion', [{ vmVersion: '1.8', value: 3 }]);
    expect(option.yAxis.inverse).toBe(true);
  });

  it('reads the category from the field matching the chart type', () => {
    const { option } = renderChart('agentVersion', [
      { agentVersion: '2.5.0', value: 7 },
      { agentVersion: '2.5.1', value: 2 },
    ]);
    expect(option.yAxis.data).toEqual(['2.5.0', '2.5.1']);
  });

  it('names the series JVM and colors it blue for vmVersion', () => {
    const { option } = renderChart('vmVersion', [{ vmVersion: '1.8', value: 3 }]);
    expect(option.series[0].name).toBe('JVM');
    expect(option.series[0].itemStyle.color).toBe(colors.blue[600]);
    expect(option.legend.data).toEqual(['JVM']);
  });

  it('names the series Agent and colors it orange for agentVersion', () => {
    const { option } = renderChart('agentVersion', [{ agentVersion: '2.5.0', value: 7 }]);
    expect(option.series[0].name).toBe('Agent');
    expect(option.series[0].itemStyle.color).toBe(colors.orange[500]);
    expect(option.legend.data).toEqual(['Agent']);
  });

  it('shows the value label at the right end of each bar', () => {
    const { option } = renderChart('vmVersion', [{ vmVersion: '1.8', value: 3 }]);
    expect(option.series[0].label).toEqual({ show: true, position: 'right', fontSize: 12 });
  });

  it('renders empty category/data lists without crashing when there is no data', () => {
    const { option } = renderChart('vmVersion', []);
    expect(option.yAxis.data).toEqual([]);
    expect(option.series[0].data).toEqual([]);
  });

  it('renders without crashing when chartData is undefined', () => {
    const { option } = renderChart('vmVersion', undefined);
    expect(option.yAxis.data).toEqual([]);
    expect(option.series[0].data).toEqual([]);
  });
});
