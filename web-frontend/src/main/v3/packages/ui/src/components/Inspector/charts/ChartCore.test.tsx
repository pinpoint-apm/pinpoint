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
  MarkPointComponent: {},
}));
jest.mock('echarts/renderers', () => ({ CanvasRenderer: {} }));
// legend 줄바꿈 행 수 계산은 canvas measureText 에 의존하는데 jsdom 에는 canvas 가 없다.
// grid.bottom 값은 이 테스트의 검증 대상이 아니므로 모듈을 모킹해 jsdom 경고를 피한다.
jest.mock('../../../lib/charts/echartsLegendLayout', () => ({
  getGridBottom: () => 60,
  LEGEND_ICON_WIDTH: 10,
  LEGEND_ITEM_GAP: 8,
  buildBottomLegend: (names: string[], extra?: Record<string, unknown>) => ({
    data: names,
    ...extra,
  }),
}));
jest.mock('../../../lib/charts/useEChartsInstance', () => ({
  useEChartsInstance: () => ({
    chartRef: mockChartRef,
    chartInstanceRef: mockChartInstanceRef,
    renderRef: mockRenderRef,
  }),
}));

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

    const gcSeries = option.series.find((s: { name: string }) => s.name === 'gcTime');
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

  it('blanks the empty-message graphic text when data exists', () => {
    const { option } = renderChart(
      makeData([{ fieldName: 'cpu', unit: 'percent', chartType: 'line', valueList: [0, 1, 2] }]),
    );
    // graphic 요소는 항상 하나 유지하되(잔존 방지), 데이터가 있으면 텍스트를 비운다.
    expect(option.graphic).toHaveLength(1);
    expect(option.graphic[0].style.text).toBe('');
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

  describe('isolated points (a value whose both neighbors are uncollected)', () => {
    // Values collected every 60 seconds on a 10-second grid: one value followed by five uncollected slots.
    const sparseValues = [-1, 5, -1, -1, -1, -1, -1, 7, -1, -1, -1, -1];
    const sparseTimestamps = sparseValues.map((_, index) => index * 10000);

    it('should mark every isolated point so that sparse data is visible', () => {
      const { option } = renderChart(
        makeData(
          [{ fieldName: 'heap', unit: 'bytes', chartType: 'line', valueList: sparseValues }],
          sparseTimestamps,
        ),
      );
      const { markPoint } = option.series[0];
      expect(markPoint.data).toEqual([{ coord: [1, 5] }, { coord: [7, 7] }]);
      expect(markPoint.symbol).toBe('circle');
      expect(markPoint.silent).toBe(true);
      expect(markPoint.label.show).toBe(false);
      // The series data is left as is, so the tooltip dataIndex stays aligned with the timestamps.
      expect(option.series[0].data).toEqual(sparseValues.map((v) => (v < 0 ? null : v)));
    });

    it('should mark an isolated point at either end of the range', () => {
      const { option } = renderChart(
        makeData(
          [{ fieldName: 'heap', unit: 'bytes', chartType: 'line', valueList: [3, -1, -1, 4] }],
          [1000, 2000, 3000, 4000],
        ),
      );
      expect(option.series[0].markPoint.data).toEqual([{ coord: [0, 3] }, { coord: [3, 4] }]);
    });

    it('should mark an isolated 0 as well', () => {
      const { option } = renderChart(
        makeData([
          { fieldName: 'cpu', unit: 'percent', chartType: 'line', valueList: [-1, 0, -1] },
        ]),
      );
      expect(option.series[0].markPoint.data).toEqual([{ coord: [1, 0] }]);
    });

    it('should not add markPoint when every value has a collected neighbor', () => {
      const { option } = renderChart(
        makeData(
          [
            {
              fieldName: 'heap',
              unit: 'bytes',
              chartType: 'line',
              valueList: [1, 2, -1, -1, 3, 4],
            },
          ],
          [1000, 2000, 3000, 4000, 5000, 6000],
        ),
      );
      expect(option.series[0].markPoint).toBeUndefined();
    });

    it('should mark only the isolated point when a series mixes segments and isolated points', () => {
      const { option } = renderChart(
        makeData(
          [
            {
              fieldName: 'heap',
              unit: 'bytes',
              chartType: 'line',
              valueList: [1, 2, -1, 9, -1, 3],
            },
          ],
          [1000, 2000, 3000, 4000, 5000, 6000],
        ),
      );
      expect(option.series[0].markPoint.data).toEqual([{ coord: [3, 9] }, { coord: [5, 3] }]);
    });

    it('should not mark a bar series, which is visible without a neighbor', () => {
      const chartOptions = {
        seriesOptions: { gcTime: { unit: 'ms', type: 'bar' } },
        yAxis: [{ unit: 'ms' }],
        legendShow: true,
      };
      const { option } = renderChart(
        makeData([{ fieldName: 'gcTime', unit: 'ms', chartType: 'bar', valueList: [-1, 5, -1] }]),
        chartOptions,
      );
      expect(option.series[0].markPoint).toBeUndefined();
    });

    it('should not mark a stacked series, whose drawn height is the cumulative value', () => {
      const chartOptions = {
        seriesOptions: { fast: { unit: 'count', stack: 'inspector-stack' } },
        yAxis: [{ unit: 'count' }],
        legendShow: true,
      };
      const { option } = renderChart(
        makeData([{ fieldName: 'fast', unit: 'count', chartType: 'line', valueList: [-1, 5, -1] }]),
        chartOptions,
      );
      expect(option.series[0].markPoint).toBeUndefined();
    });
  });
});
