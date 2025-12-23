import { useChartParseData } from './useChartParseData';
import { InspectorAgentChart } from '@pinpoint-fe/ui/src/constants';

describe('useChartParseData', () => {
  it('should return chartMetricData, chartTooltipData, and dataKeys', () => {
    const result = useChartParseData([]);

    expect(result).toHaveProperty('chartMetricData');
    expect(result).toHaveProperty('chartTooltipData');
    expect(result).toHaveProperty('dataKeys');
    expect(Array.isArray(result.chartMetricData)).toBe(true);
    expect(Array.isArray(result.chartTooltipData)).toBe(true);
    expect(Array.isArray(result.dataKeys)).toBe(true);
  });

  it('should handle empty array', () => {
    const result = useChartParseData([]);

    expect(result.chartMetricData).toEqual([]);
    expect(result.chartTooltipData).toEqual([]);
    expect(result.dataKeys).toEqual([]);
  });

  it('should handle undefined input', () => {
    const result = useChartParseData(undefined);

    expect(result.chartMetricData).toEqual([]);
    expect(result.chartTooltipData).toEqual([]);
    expect(result.dataKeys).toEqual([]);
  });

  it('should separate metric data from tooltip data', () => {
    const data: InspectorAgentChart.MetricValue[] = [
      {
        chartType: 'line',
        fieldName: 'metric1',
        unit: 'count',
        valueList: [1, 2, 3],
      },
      {
        chartType: 'tooltip',
        fieldName: 'tooltip1',
        unit: 'count',
        valueList: [4, 5, 6],
      },
    ];

    const result = useChartParseData(data);

    expect(result.chartMetricData).toHaveLength(1);
    expect(result.chartMetricData[0].fieldName).toBe('metric1');
    expect(result.chartTooltipData).toHaveLength(1);
    expect(result.chartTooltipData[0].fieldName).toBe('tooltip1');
  });

  it('should collect stacking data field names', () => {
    const data: InspectorAgentChart.MetricValue[] = [
      {
        chartType: 'line',
        fieldName: 'fastCount',
        unit: 'count',
        valueList: [1, 2, 3],
      },
      {
        chartType: 'line',
        fieldName: 'normalCount',
        unit: 'count',
        valueList: [4, 5, 6],
      },
      {
        chartType: 'line',
        fieldName: 'otherField',
        unit: 'count',
        valueList: [7, 8, 9],
      },
    ];

    const result = useChartParseData(data);

    expect(result.dataKeys).toContain('fastCount');
    expect(result.dataKeys).toContain('normalCount');
    expect(result.dataKeys).not.toContain('otherField');
  });

  it('should collect all stacking data field names', () => {
    const stackingFields = [
      'fastCount',
      'normalCount',
      'slowCount',
      'verySlowCount',
      'sampledNewCount',
      'sampledContinuationCount',
      'unsampledNewCount',
      'unsampledContinuationCount',
      'skippedNewSkipCount',
      'skippedContinuationCount',
    ];

    const data: InspectorAgentChart.MetricValue[] = stackingFields.map((fieldName) => ({
      chartType: 'line',
      fieldName,
      unit: 'count',
      valueList: [1, 2, 3],
    }));

    const result = useChartParseData(data);

    expect(result.dataKeys).toHaveLength(stackingFields.length);
    stackingFields.forEach((field) => {
      expect(result.dataKeys).toContain(field);
    });
  });

  it('should handle mixed metric and tooltip data with stacking fields', () => {
    const data: InspectorAgentChart.MetricValue[] = [
      {
        chartType: 'line',
        fieldName: 'fastCount',
        unit: 'count',
        valueList: [1, 2, 3],
      },
      {
        chartType: 'tooltip',
        fieldName: 'tooltip1',
        unit: 'count',
        valueList: [4, 5, 6],
      },
      {
        chartType: 'spline',
        fieldName: 'normalCount',
        unit: 'count',
        valueList: [7, 8, 9],
      },
    ];

    const result = useChartParseData(data);

    expect(result.chartMetricData).toHaveLength(2);
    expect(result.chartTooltipData).toHaveLength(1);
    expect(result.dataKeys).toContain('fastCount');
    expect(result.dataKeys).toContain('normalCount');
    expect(result.dataKeys).not.toContain('tooltip1');
  });
});

