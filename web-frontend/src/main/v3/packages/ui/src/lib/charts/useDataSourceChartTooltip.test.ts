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

    expect(tooltipTitleList).toEqual(['Jdbc URL', 'ServiceType', 'Active Avg', 'Active Max', 'Total Max']);
  });

  it('should return empty array when tooltipData is empty', () => {
    const { getTooltipData } = useDataSourceChartTooltip([]);

    const result = getTooltipData(0);

    expect(result).toEqual([]);
  });

  it('should handle undefined tooltipData', () => {
    const { getTooltipData } = useDataSourceChartTooltip(undefined);

    const result = getTooltipData(0);

    expect(result).toEqual([]);
  });

  it('should extract tooltip data for given focusIndex', () => {
    const tooltipData: InspectorAgentDataSourceChart.MetricValueGroup[] = [
      {
        metricValues: [
          {
            chartType: 'line',
            fieldName: 'activeAvg',
            unit: 'count',
            valueList: [10, 20, 30],
          },
          {
            chartType: 'line',
            fieldName: 'activeMax',
            unit: 'count',
            valueList: [40, 50, 60],
          },
          {
            chartType: 'line',
            fieldName: 'totalMax',
            unit: 'count',
            valueList: [70, 80, 90],
          },
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
          {
            chartType: 'line',
            fieldName: 'activeAvg',
            unit: 'count',
            valueList: [10],
          },
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

  it('should generate tooltip HTML string with title list and contents', () => {
    const { getTooltipStr } = useDataSourceChartTooltip([]);

    const titleList = ['Time', 'Jdbc URL', 'ServiceType'];
    const contentsData = [
      {
        id: 'id1',
        values: [
          { jdbcUrl: 'jdbc:mysql://localhost:3306/test', serviceType: 'MYSQL', activeAvg: 10 },
        ],
        color: '#ff0000',
        name: 'Database1',
      },
    ];

    const result = getTooltipStr(titleList, contentsData);

    expect(result).toContain('Time');
    expect(result).toContain('Jdbc URL');
    expect(result).toContain('ServiceType');
    expect(result).toContain('Database1');
    expect(result).toContain('#ff0000');
    expect(result).toContain('<table');
    expect(result).toContain('<tbody>');
  });

  it('should generate tooltip with correct HTML structure', () => {
    const { getTooltipStr } = useDataSourceChartTooltip([]);

    const titleList = ['Title1', 'Title2'];
    const contentsData = [
      {
        id: 'id1',
        values: [{ value1: 'val1', value2: 'val2' }],
        color: '#ff0000',
        name: 'Name1',
      },
    ];

    const result = getTooltipStr(titleList, contentsData);

    expect(result).toContain('<th>Title1</th>');
    expect(result).toContain('<th>Title2</th>');
    expect(result).toContain('bb-tooltip-name-id1');
    expect(result).toContain('class="name"');
    expect(result).toContain('class="value"');
  });

  it('should handle multiple metric value groups', () => {
    const tooltipData: InspectorAgentDataSourceChart.MetricValueGroup[] = [
      {
        metricValues: [
          {
            chartType: 'line',
            fieldName: 'activeAvg',
            unit: 'count',
            valueList: [10, 20],
          },
        ],
        tags: [{ name: 'id', value: 'id1' }],
      },
      {
        metricValues: [
          {
            chartType: 'line',
            fieldName: 'activeAvg',
            unit: 'count',
            valueList: [30, 40],
          },
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

  it('should handle empty contentsData in getTooltipStr', () => {
    const { getTooltipStr } = useDataSourceChartTooltip([]);

    const titleList = ['Title1', 'Title2'];
    const result = getTooltipStr(titleList, []);

    expect(result).toContain('Title1');
    expect(result).toContain('Title2');
    expect(result).toContain('<table');
    expect(result).toContain('<tbody>');
  });
});

