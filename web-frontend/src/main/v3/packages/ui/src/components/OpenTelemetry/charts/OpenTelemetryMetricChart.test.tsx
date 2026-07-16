import { render, screen } from '@testing-library/react';
import { Chart, MetricValue, MetricValueGroup } from '@pinpoint-fe/ui/src/constants';
import { CHART_SERIES_COLORS } from '../../../lib/colors';

// echarts 자체와 인스턴스 라이프사이클 훅은 모킹해, OpenTelemetryMetricChart 가 chart.setOption 에
// 넘기는 옵션(순수 변환 로직)만 검증한다. (실제 렌더/canvas 는 이 테스트의 관심사가 아니다)
const mockSetOption = jest.fn();
const mockChartRef = { current: null as HTMLDivElement | null };
const mockChartInstanceRef = { current: { setOption: mockSetOption } };

jest.mock('echarts/core', () => ({ use: jest.fn() }));
jest.mock('echarts/charts', () => ({ BarChart: {}, LineChart: {} }));
jest.mock('echarts/components', () => ({
  GridComponent: {},
  TooltipComponent: {},
  LegendComponent: {},
  GraphicComponent: {},
}));
jest.mock('echarts/renderers', () => ({ CanvasRenderer: {} }));
jest.mock('../../../lib/charts/useEChartsInstance', () => ({
  useEChartsInstance: () => ({
    chartRef: mockChartRef,
    chartInstanceRef: mockChartInstanceRef,
    renderRef: { current: null },
  }),
}));

// eslint-disable-next-line import/first
import { OpenTelemetryMetricChart } from './OpenTelemetryMetricChart';

const makeMetricValue = (fieldName: string, values: number[]): MetricValue => ({
  fieldName,
  values,
});

const makeGroup = (chartType: string, metricValues: MetricValue[], unit = ''): MetricValueGroup => ({
  groupName: 'group',
  chartType,
  unit,
  metricValues,
});

const makeChart = (
  metricValueGroups: MetricValueGroup[],
  timestamp: number[] = [1000, 2000, 3000],
): Chart => ({ title: 'Test', timestamp, metricValueGroups });

const renderChart = (
  chart: Chart,
  props: Partial<React.ComponentProps<typeof OpenTelemetryMetricChart>> = {},
) => {
  render(<OpenTelemetryMetricChart chartData={chart} {...props} />);
  const [option, mergeArg] = mockSetOption.mock.calls[0];
  return { option, mergeArg };
};

describe('OpenTelemetryMetricChart', () => {
  beforeEach(() => {
    mockSetOption.mockClear();
  });

  it('calls setOption with replaceMerge series so stale series do not linger', () => {
    const { mergeArg } = renderChart(makeChart([makeGroup('line', [makeMetricValue('cpu', [1, 2, 3])])]));
    expect(mergeArg).toEqual({ replaceMerge: ['series'] });
  });

  it('enables animation by default and disables it when isAnimationActive is false', () => {
    const chart = makeChart([makeGroup('line', [makeMetricValue('cpu', [1, 2, 3])])]);
    expect(renderChart(chart).option.animation).toBe(true);
    mockSetOption.mockClear();
    expect(renderChart(chart, { isAnimationActive: false }).option.animation).toBe(false);
  });

  it('maps a bar chartType to a bar series without smoothing or area fill', () => {
    const { option } = renderChart(makeChart([makeGroup('bar', [makeMetricValue('cpu', [1, 2, 3])])]));
    expect(option.series[0].type).toBe('bar');
    expect(option.series[0].smooth).toBe(false);
    expect(option.series[0].areaStyle).toBeUndefined();
  });

  it('maps an area chartType to a smoothed line series with an area fill', () => {
    const { option } = renderChart(makeChart([makeGroup('area', [makeMetricValue('cpu', [1, 2, 3])])]));
    expect(option.series[0].type).toBe('line');
    expect(option.series[0].smooth).toBe(true);
    expect(option.series[0].areaStyle).toEqual({ opacity: 0.4 });
  });

  it('converts -1/null values to null so the line breaks (keeping 0/positive)', () => {
    const { option } = renderChart(makeChart([makeGroup('line', [makeMetricValue('cpu', [-1, 0, 5])])]));
    expect(option.series[0].data).toEqual([null, 0, 5]);
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

  it('builds the category x axis from timestamps', () => {
    const { option } = renderChart(
      makeChart([makeGroup('line', [makeMetricValue('cpu', [1, 2, 3])])], [1000, 2000, 3000]),
    );
    expect(option.xAxis.type).toBe('category');
    expect(option.xAxis.data).toEqual([1000, 2000, 3000]);
  });

  it('uses boundaryGap only when a bar series is present', () => {
    expect(
      renderChart(makeChart([makeGroup('line', [makeMetricValue('cpu', [1, 2, 3])])])).option.xAxis
        .boundaryGap,
    ).toBe(false);
    mockSetOption.mockClear();
    expect(
      renderChart(makeChart([makeGroup('bar', [makeMetricValue('cpu', [1, 2, 3])])])).option.xAxis
        .boundaryGap,
    ).toBe(true);
  });

  it('shows the unit as the y axis name only when a unit is provided', () => {
    expect(
      renderChart(makeChart([makeGroup('line', [makeMetricValue('cpu', [1, 2, 3])])]), {
        unit: 'percent',
      }).option.yAxis.name,
    ).toBe('percent');
    mockSetOption.mockClear();
    expect(
      renderChart(makeChart([makeGroup('line', [makeMetricValue('cpu', [1, 2, 3])])])).option.yAxis
        .name,
    ).toBeUndefined();
  });

  it('hides the y axis horizontal gridlines but keeps axis lines and ticks on both axes', () => {
    const { option } = renderChart(makeChart([makeGroup('line', [makeMetricValue('cpu', [1, 2, 3])])]));
    expect(option.yAxis.splitLine).toEqual({ show: false });
    expect(option.yAxis.axisLine).toEqual({ show: true });
    expect(option.yAxis.axisTick).toEqual({ show: true });
    expect(option.xAxis.axisLine).toEqual({ show: true });
    expect(option.xAxis.axisTick.show).toBe(true);
  });

  it('renders a No Data graphic when there is no collected value', () => {
    const empty = renderChart(makeChart([makeGroup('line', [makeMetricValue('cpu', [-1, -1, -1])])]));
    expect(empty.option.graphic[0].style.text).toBe('No Data');
    mockSetOption.mockClear();
    const withData = renderChart(makeChart([makeGroup('line', [makeMetricValue('cpu', [1, 2, 3])])]));
    expect(withData.option.graphic[0].style.text).toBe('');
  });

  it('does not use the echarts built-in legend (custom React legend is rendered instead)', () => {
    const { option } = renderChart(makeChart([makeGroup('line', [makeMetricValue('cpu', [1, 2, 3])])]));
    expect(option.legend).toBeUndefined();
  });

  it('renders a custom legend carrying the full (untruncated) series name in the DOM', () => {
    const longName = 'telemetry.sdk.language:java,spring.security.reached.filter.name:none';
    renderChart(makeChart([makeGroup('line', [makeMetricValue(longName, [1, 2, 3])])]));
    // CSS 로만 말줄임되므로 DOM 에는 전체 이름이 그대로 있어, hover 시 전체를 볼 수 있다.
    expect(screen.getByText(longName)).toBeTruthy();
  });

  describe('tooltip formatter', () => {
    const params = [
      { axisValue: 1000, value: 3, seriesName: 'cpu', color: '#f00' },
      { axisValue: 1000, value: 5, seriesName: 'mem', color: '#0f0' },
    ];

    it('renders one row per series with the formatted value', () => {
      const { option } = renderChart(makeChart([makeGroup('line', [makeMetricValue('cpu', [3])])]));
      const html = option.tooltip.formatter(params);
      expect(html).toContain('cpu');
      expect(html).toContain('mem');
      expect(html).toContain('>3<');
      expect(html).toContain('>5<');
      expect(html).not.toContain('total');
    });

    it('appends a summed total row when showTotal is set', () => {
      const { option } = renderChart(makeChart([makeGroup('line', [makeMetricValue('cpu', [3])])]), {
        tooltipConfig: { showTotal: true },
      });
      const html = option.tooltip.formatter(params);
      expect(html).toContain('total');
      expect(html).toContain('>8<');
    });

    it('skips null-valued series and returns empty string when nothing is collected', () => {
      const { option } = renderChart(makeChart([makeGroup('line', [makeMetricValue('cpu', [3])])]));
      expect(option.tooltip.formatter([{ axisValue: 1000, value: null, seriesName: 'cpu' }])).toBe('');
    });

    it('truncates long series names with an ellipsis (value stays visible)', () => {
      const { option } = renderChart(makeChart([makeGroup('line', [makeMetricValue('cpu', [3])])]));
      const html = option.tooltip.formatter([
        { axisValue: 1000, value: 42, seriesName: 'a.very.long.series.name.that.overflows', color: '#f00' },
      ]);
      // inline-block 이어야 max-width/overflow/ellipsis 가 실제로 적용된다.
      expect(html).toContain('display: inline-block');
      expect(html).toContain('text-overflow: ellipsis');
      expect(html).toContain('a.very.long.series.name.that.overflows');
      expect(html).toContain('>42<');
    });

    it('confines the tooltip to the chart widget', () => {
      const { option } = renderChart(makeChart([makeGroup('line', [makeMetricValue('cpu', [3])])]));
      expect(option.tooltip.confine).toBe(true);
    });
  });
});
