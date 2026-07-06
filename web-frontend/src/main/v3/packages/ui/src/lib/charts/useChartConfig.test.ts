import { useChartConfig, DEFAULT_CHART_CONFIG, INSPECTOR_CHART_GROUP } from './useChartConfig';
import { InspectorAgentChart } from '@pinpoint-fe/ui/src/constants';

const makeData = (
  overrides: Partial<InspectorAgentChart.Response> = {},
): InspectorAgentChart.Response => ({
  title: 'Test Chart',
  timestamp: [1000, 2000, 3000],
  metricValues: [
    { chartType: 'line', fieldName: 'field1', unit: 'count', valueList: [10, 20, 30] },
  ],
  ...overrides,
});

describe('useChartConfig', () => {
  describe('DEFAULT_CHART_CONFIG', () => {
    it('should expose the default max value', () => {
      expect(DEFAULT_CHART_CONFIG.DEFAULT_MAX).toBe(10);
    });
  });

  it('should return null when data is null', () => {
    expect(useChartConfig(null)).toBeNull();
  });

  it('should return null when data is undefined', () => {
    expect(useChartConfig(undefined)).toBeNull();
  });

  it('should return chartData and chartOptions when data is provided', () => {
    const result = useChartConfig(makeData());

    expect(result).not.toBeNull();
    expect(result).toHaveProperty('chartData');
    expect(result).toHaveProperty('chartOptions');
  });

  it('should include title and timestamp in chartData', () => {
    const result = useChartConfig(makeData());

    expect(result?.chartData.title).toBe('Test Chart');
    expect(result?.chartData.timestamp).toEqual([1000, 2000, 3000]);
  });

  it('should build a series option per rendered field with type and unit', () => {
    const result = useChartConfig(
      makeData({
        metricValues: [
          { chartType: 'spline', fieldName: 'field1', unit: 'count', valueList: [10, 20, 30] },
          { chartType: 'bar', fieldName: 'field2', unit: 'percent', valueList: [40, 50, 60] },
        ],
      }),
    );

    expect(result?.chartOptions.seriesOptions.field1).toMatchObject({
      type: 'line',
      smooth: true,
      unit: 'count',
    });
    expect(result?.chartOptions.seriesOptions.field2).toMatchObject({
      type: 'bar',
      unit: 'percent',
    });
  });

  it('should create one y-axis per distinct unit', () => {
    const result = useChartConfig(
      makeData({
        metricValues: [
          { chartType: 'line', fieldName: 'field1', unit: 'unit1', valueList: [10, 20, 30] },
          { chartType: 'line', fieldName: 'field2', unit: 'unit2', valueList: [40, 50, 60] },
          { chartType: 'line', fieldName: 'field3', unit: 'unit1', valueList: [1, 2, 3] },
        ],
      }),
    );

    expect(result?.chartOptions.yAxis).toEqual([{ unit: 'unit1' }, { unit: 'unit2' }]);
  });

  it('should mark stacking fields with a stack group', () => {
    const result = useChartConfig(
      makeData({
        metricValues: [
          { chartType: 'line', fieldName: 'fastCount', unit: 'count', valueList: [10, 20, 30] },
          { chartType: 'line', fieldName: 'normalCount', unit: 'count', valueList: [40, 50, 60] },
        ],
      }),
    );

    expect(result?.chartOptions.seriesOptions.fastCount.stack).toBeDefined();
    expect(result?.chartOptions.seriesOptions.normalCount.stack).toBe(
      result?.chartOptions.seriesOptions.fastCount.stack,
    );
  });

  it('should not group non-stacking fields', () => {
    const result = useChartConfig(makeData());

    expect(result?.chartOptions.seriesOptions.field1.stack).toBeUndefined();
  });

  it('should not link tooltip (no group) for Apdex Score', () => {
    const result = useChartConfig(makeData({ title: 'Apdex Score' }));

    expect(result?.chartOptions.group).toBeUndefined();
  });

  it('should link tooltip via a shared group for other charts', () => {
    const result = useChartConfig(makeData({ title: 'Other Chart' }));

    expect(result?.chartOptions.group).toBe(INSPECTOR_CHART_GROUP);
  });

  it('should apply colors from init options to series', () => {
    const result = useChartConfig(makeData(), { colors: { field1: '#ff0000' } });

    expect(result?.chartOptions.seriesOptions.field1.color).toBe('#ff0000');
  });

  it('should mark dashed fields from init options', () => {
    const result = useChartConfig(makeData(), { dashedFields: ['field1'] });

    expect(result?.chartOptions.seriesOptions.field1.dashed).toBe(true);
  });

  it('should show legend by default and hide it when requested', () => {
    expect(useChartConfig(makeData())?.chartOptions.legendShow).toBe(true);
    expect(useChartConfig(makeData(), { legendShow: false })?.chartOptions.legendShow).toBe(false);
  });

  it('should exclude tooltip-only fields from rendered chartData', () => {
    const result = useChartConfig(
      makeData({
        metricValues: [
          { chartType: 'line', fieldName: 'field1', unit: 'count', valueList: [10, 20, 30] },
          { chartType: 'tooltip', fieldName: 'extra', unit: 'count', valueList: [1, 2, 3] },
        ],
      }),
    );

    expect(result?.chartData.metricValues.map((mv) => mv.fieldName)).toEqual(['field1']);
    expect(result?.chartOptions.seriesOptions.extra).toBeUndefined();
  });
});
