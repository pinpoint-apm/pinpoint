import { useDataSourceChartConfig, DATA_SOURCE_TOOLTIP_ID } from './useDataSourceChartConfig';
import { InspectorAgentDataSourceChart } from '@pinpoint-fe/ui/src/constants';
import { AxisTooltipParam } from './useChartTooltip';
import { INSPECTOR_CHART_GROUP } from './useChartConfig';

const makeData = (
  groups: InspectorAgentDataSourceChart.MetricValueGroup[],
): InspectorAgentDataSourceChart.Response => ({
  title: 'Test Chart',
  timestamp: [1000, 2000, 3000],
  metricValueGroups: groups,
});

const group = (
  databaseName: string | undefined,
  id: string | undefined,
  valueList: number[] = [10, 20, 30],
): InspectorAgentDataSourceChart.MetricValueGroup => ({
  metricValues: [{ chartType: 'spline', fieldName: 'activeAvg', unit: 'count', valueList }],
  tags: [
    ...(id !== undefined ? [{ name: 'id', value: id }] : []),
    ...(databaseName !== undefined ? [{ name: 'databaseName', value: databaseName }] : []),
  ],
});

describe('useDataSourceChartConfig', () => {
  it('should return null when data is null', () => {
    expect(useDataSourceChartConfig(null)).toBeNull();
  });

  it('should return null when data is undefined', () => {
    expect(useDataSourceChartConfig(undefined)).toBeNull();
  });

  it('should return chartData and chartOptions when data is provided', () => {
    const result = useDataSourceChartConfig(makeData([group('Database1', 'id1')]));

    expect(result).not.toBeNull();
    expect(result).toHaveProperty('chartData');
    expect(result).toHaveProperty('chartOptions');
  });

  it('should include title and timestamp in chartData', () => {
    const result = useDataSourceChartConfig(makeData([group('Database1', 'id1')]));

    expect(result?.chartData.title).toBe('Test Chart');
    expect(result?.chartData.timestamp).toEqual([1000, 2000, 3000]);
  });

  it('should create fieldName from databaseName and id', () => {
    const result = useDataSourceChartConfig(makeData([group('Database1', 'id1')]));

    expect(result?.chartData.metricValues[0].fieldName).toBe('Database1-id1');
    expect(result?.chartData.metricValues[0].dataLabel).toBe('Database1');
  });

  it('should use index as id when id tag is missing', () => {
    const result = useDataSourceChartConfig(makeData([group('Database1', undefined)]));

    expect(result?.chartData.metricValues[0].fieldName).toBe('Database1-0');
  });

  it('should use "Database" as default databaseName when missing', () => {
    const result = useDataSourceChartConfig(makeData([group(undefined, 'id1')]));

    expect(result?.chartData.metricValues[0].fieldName).toBe('Database-id1');
    expect(result?.chartData.metricValues[0].dataLabel).toBe('Database');
  });

  it('should build series options keyed by fieldName with type, unit and display name', () => {
    const result = useDataSourceChartConfig(makeData([group('Database1', 'id1')]));

    expect(result?.chartOptions.seriesOptions['Database1-id1']).toMatchObject({
      type: 'line',
      smooth: true,
      unit: 'count',
      name: 'Database1',
    });
  });

  it('should configure a single y-axis named "Active Avg"', () => {
    const result = useDataSourceChartConfig(makeData([group('Database1', 'id1')]));

    expect(result?.chartOptions.yAxis).toEqual([{ unit: 'count', name: 'Active Avg' }]);
  });

  it('should hide the legend', () => {
    const result = useDataSourceChartConfig(makeData([group('Database1', 'id1')]));

    expect(result?.chartOptions.legendShow).toBe(false);
  });

  it('should link the tooltip to other inspector charts via the shared group', () => {
    const result = useDataSourceChartConfig(makeData([group('Database1', 'id1')]));

    expect(result?.chartOptions.group).toBe(INSPECTOR_CHART_GROUP);
  });

  it('should handle multiple metric value groups', () => {
    const result = useDataSourceChartConfig(
      makeData([group('Database1', 'id1'), group('Database2', 'id2', [40, 50, 60])]),
    );

    expect(result?.chartData.metricValues).toHaveLength(2);
    expect(result?.chartData.metricValues[0].fieldName).toBe('Database1-id1');
    expect(result?.chartData.metricValues[1].fieldName).toBe('Database2-id2');
  });

  describe('tooltipFormatter', () => {
    it('should be a function', () => {
      const result = useDataSourceChartConfig(makeData([group('Database1', 'id1')]));

      expect(typeof result?.chartOptions.tooltipFormatter).toBe('function');
    });

    it('should render a table into the external tooltip element and return empty string', () => {
      const element = { innerHTML: '' };
      const querySelectorSpy = jest
        .spyOn(document, 'querySelector')
        .mockImplementation((selector) =>
          selector === `#${DATA_SOURCE_TOOLTIP_ID}` ? (element as unknown as Element) : null,
        );

      const data = makeData([
        {
          metricValues: [
            { chartType: 'spline', fieldName: 'activeAvg', unit: 'count', valueList: [10, 20, 30] },
          ],
          tags: [
            { name: 'id', value: 'id1' },
            { name: 'databaseName', value: 'Database1' },
            { name: 'jdbcUrl', value: 'jdbc:mysql://localhost:3306/test' },
            { name: 'serviceType', value: 'MYSQL' },
          ],
        },
      ]);
      const result = useDataSourceChartConfig(data);

      const params: AxisTooltipParam[] = [
        {
          axisValue: 2000,
          dataIndex: 1,
          seriesIndex: 0,
          seriesName: 'Database1',
          color: '#ff0000',
        },
      ];
      const returned = result?.chartOptions.tooltipFormatter?.(params);

      expect(returned).toBe('');
      expect(element.innerHTML).toContain('Database1');
      expect(element.innerHTML).toContain('jdbc:mysql://localhost:3306/test');
      expect(element.innerHTML).toContain('#ff0000');

      querySelectorSpy.mockRestore();
    });

    it('should return empty string when the tooltip element is missing', () => {
      const querySelectorSpy = jest.spyOn(document, 'querySelector').mockReturnValue(null);
      const result = useDataSourceChartConfig(makeData([group('Database1', 'id1')]));

      expect(result?.chartOptions.tooltipFormatter?.([{ dataIndex: 0 }])).toBe('');

      querySelectorSpy.mockRestore();
    });
  });
});
