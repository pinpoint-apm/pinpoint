import { useDataSourceChartTooltip } from './useDataSourceChartTooltip';
import { InspectorAgentDataSourceChart } from '@pinpoint-fe/ui/src/constants';

describe('useDataSourceChartTooltip', () => {
  it('should return getTooltipData, getTooltipStr, and tooltipTitleList', () => {
    const { getTooltipData, getTooltipStr, tooltipTitleList } = useDataSourceChartTooltip([]);

    expect(typeof getTooltipData).toBe('function');
    expect(typeof getTooltipStr).toBe('function');
    expect(Array.isArray(tooltipTitleList)).toBe(true);
  });

  it('should have correct tooltipTitleList', () => {
    const { tooltipTitleList } = useDataSourceChartTooltip([]);

    expect(tooltipTitleList).toEqual([
      'Jdbc URL',
      'ServiceType',
      'Active Avg',
      'Active Max',
      'Total Max',
    ]);
  });

  it('should return empty array when tooltipData is empty', () => {
    const { getTooltipData } = useDataSourceChartTooltip([]);

    expect(getTooltipData(0)).toEqual([]);
  });

  it('should handle undefined tooltipData', () => {
    const { getTooltipData } = useDataSourceChartTooltip(undefined);

    expect(getTooltipData(0)).toEqual([]);
  });

  it('should extract tooltip data for given focusIndex', () => {
    const tooltipData: InspectorAgentDataSourceChart.MetricValueGroup[] = [
      {
        metricValues: [
          { chartType: 'line', fieldName: 'activeAvg', unit: 'count', valueList: [10, 20, 30] },
          { chartType: 'line', fieldName: 'activeMax', unit: 'count', valueList: [40, 50, 60] },
          { chartType: 'line', fieldName: 'totalMax', unit: 'count', valueList: [70, 80, 90] },
        ],
        tags: [
          { name: 'jdbcUrl', value: 'jdbc:mysql://localhost:3306/test' },
          { name: 'serviceType', value: 'MYSQL' },
          { name: 'id', value: 'id1' },
        ],
      },
    ];

    const { getTooltipData } = useDataSourceChartTooltip(tooltipData);

    const result = getTooltipData(1);

    expect(result).toHaveLength(1);
    expect(result[0]).toMatchObject({
      jdbcUrl: 'jdbc:mysql://localhost:3306/test',
      serviceType: 'MYSQL',
      activeAvg: 20,
      activeMax: 50,
      totalMax: 80,
    });
  });

  it('should handle missing tag values', () => {
    const tooltipData: InspectorAgentDataSourceChart.MetricValueGroup[] = [
      {
        metricValues: [
          { chartType: 'line', fieldName: 'activeAvg', unit: 'count', valueList: [10] },
        ],
        tags: [{ name: 'id', value: 'id1' }],
      },
    ];

    const { getTooltipData } = useDataSourceChartTooltip(tooltipData);

    const result = getTooltipData(0);

    expect(result[0].jdbcUrl).toBeUndefined();
    expect(result[0].serviceType).toBeUndefined();
    expect(result[0].activeAvg).toBe(10);
  });

  it('should handle missing metric values', () => {
    const tooltipData: InspectorAgentDataSourceChart.MetricValueGroup[] = [
      {
        metricValues: [],
        tags: [
          { name: 'jdbcUrl', value: 'jdbc:mysql://localhost:3306/test' },
          { name: 'serviceType', value: 'MYSQL' },
        ],
      },
    ];

    const { getTooltipData } = useDataSourceChartTooltip(tooltipData);

    const result = getTooltipData(0);

    expect(result[0].jdbcUrl).toBe('jdbc:mysql://localhost:3306/test');
    expect(result[0].serviceType).toBe('MYSQL');
    expect(result[0].activeAvg).toBeUndefined();
  });

  it('should handle multiple metric value groups', () => {
    const tooltipData: InspectorAgentDataSourceChart.MetricValueGroup[] = [
      {
        metricValues: [
          { chartType: 'line', fieldName: 'activeAvg', unit: 'count', valueList: [10, 20] },
        ],
        tags: [{ name: 'id', value: 'id1' }],
      },
      {
        metricValues: [
          { chartType: 'line', fieldName: 'activeAvg', unit: 'count', valueList: [30, 40] },
        ],
        tags: [{ name: 'id', value: 'id2' }],
      },
    ];

    const { getTooltipData } = useDataSourceChartTooltip(tooltipData);

    const result = getTooltipData(1);

    expect(result).toHaveLength(2);
    expect(result[0].activeAvg).toBe(20);
    expect(result[1].activeAvg).toBe(40);
  });

  describe('getTooltipStr', () => {
    it('should render header titles, row name, color, and values', () => {
      const { getTooltipStr } = useDataSourceChartTooltip([]);

      const result = getTooltipStr(
        ['', 'Jdbc URL', 'ServiceType', 'Active Avg', 'Active Max', 'Total Max'],
        [
          {
            name: 'Database1',
            color: '#ff0000',
            values: {
              jdbcUrl: 'jdbc:mysql://localhost:3306/test',
              serviceType: 'MYSQL',
              activeAvg: 10,
              activeMax: 20,
              totalMax: 30,
            },
          },
        ],
      );

      expect(result).toContain('<th>Jdbc URL</th>');
      expect(result).toContain('<th>ServiceType</th>');
      expect(result).toContain('Database1');
      expect(result).toContain('#ff0000');
      expect(result).toContain('jdbc:mysql://localhost:3306/test');
      expect(result).toContain('class="name"');
      expect(result).toContain('class="value"');
      expect(result).toContain('<table');
      expect(result).toContain('<tbody>');
    });

    it('should escape HTML in names and values to prevent injection', () => {
      const { getTooltipStr } = useDataSourceChartTooltip([]);

      const result = getTooltipStr(
        [''],
        [{ name: '<b>x</b>', color: '#000', values: { jdbcUrl: '<script>alert(1)</script>' } }],
      );

      expect(result).not.toContain('<b>x</b>');
      expect(result).not.toContain('<script>alert(1)</script>');
      expect(result).toContain('&lt;b&gt;x&lt;/b&gt;');
    });

    it('should handle empty rows', () => {
      const { getTooltipStr } = useDataSourceChartTooltip([]);

      const result = getTooltipStr(['Title1', 'Title2'], []);

      expect(result).toContain('<th>Title1</th>');
      expect(result).toContain('<th>Title2</th>');
      expect(result).toContain('<table');
      expect(result).toContain('<tbody>');
    });

    it('should render missing values as empty cells', () => {
      const { getTooltipStr } = useDataSourceChartTooltip([]);

      const result = getTooltipStr([''], [{ name: 'DB', color: '#000', values: {} }]);

      expect(result).toContain('DB');
      expect(result).not.toContain('undefined');
    });
  });
});
