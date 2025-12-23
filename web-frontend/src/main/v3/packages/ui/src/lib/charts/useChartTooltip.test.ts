import { useChartTooltip } from './useChartTooltip';
import { InspectorAgentChart } from '@pinpoint-fe/ui/src/constants';

// Mock getFormat function
jest.mock('@pinpoint-fe/ui/src/utils', () => ({
  getFormat: jest.fn((unit: string) => {
    const formatters: Record<string, (v: number) => string> = {
      count: (v: number) => v.toLocaleString(),
      percent: (v: number) => `${v}%`,
      bytes: (v: number) => `${v} bytes`,
      default: (v: number) => `${v}`,
    };
    return formatters[unit] || formatters.default;
  }),
}));

describe('useChartTooltip', () => {
  it('should return getTooltipData and getTooltipStr functions', () => {
    const { getTooltipData, getTooltipStr } = useChartTooltip([]);

    expect(typeof getTooltipData).toBe('function');
    expect(typeof getTooltipStr).toBe('function');
  });

  it('should return empty array when tooltipData is empty', () => {
    const { getTooltipData } = useChartTooltip([]);

    const result = getTooltipData(0);

    expect(result).toEqual([]);
  });

  it('should return formatted tooltip data for given index', () => {
    const tooltipData: InspectorAgentChart.MetricValue[] = [
      {
        chartType: 'tooltip',
        fieldName: 'field1',
        unit: 'count',
        valueList: [10, 20, 30],
      },
      {
        chartType: 'tooltip',
        fieldName: 'field2',
        unit: 'percent',
        valueList: [50, 60, 70],
      },
    ];

    const { getTooltipData } = useChartTooltip(tooltipData);

    const result = getTooltipData(1);

    expect(result).toHaveLength(2);
    expect(result[0]).toMatchObject({
      id: 'field1',
      color: '#f87171',
    });
    expect(result[1]).toMatchObject({
      id: 'field2',
      color: '#f87171',
    });
  });

  it('should format values using getFormat function', () => {
    const tooltipData: InspectorAgentChart.MetricValue[] = [
      {
        chartType: 'tooltip',
        fieldName: 'field1',
        unit: 'count',
        valueList: [1000, 2000],
      },
    ];

    const { getTooltipData } = useChartTooltip(tooltipData);

    const result = getTooltipData(0);

    expect(result[0].value).toBeDefined();
    expect(typeof result[0].value).toBe('string');
  });

  it('should generate tooltip HTML string with title and contents', () => {
    const { getTooltipStr } = useChartTooltip([]);

    const contentsData = [
      { id: 'field1', value: '100', color: '#ff0000' },
      { id: 'field2', value: '200', color: '#00ff00' },
    ];

    const result = getTooltipStr('Test Title', contentsData);

    expect(result).toContain('Test Title');
    expect(result).toContain('field1');
    expect(result).toContain('field2');
    expect(result).toContain('100');
    expect(result).toContain('200');
    expect(result).toContain('#ff0000');
    expect(result).toContain('#00ff00');
    expect(result).toContain('<table');
    expect(result).toContain('<tbody>');
  });

  it('should generate tooltip with correct HTML structure', () => {
    const { getTooltipStr } = useChartTooltip([]);

    const contentsData = [
      { id: 'field1', value: '100', color: '#ff0000' },
    ];

    const result = getTooltipStr('Title', contentsData);

    expect(result).toContain('<tr><th colspan="2">Title</th></tr>');
    expect(result).toContain('bb-tooltip-name-field1');
    expect(result).toContain('class="name"');
    expect(result).toContain('class="value"');
  });

  it('should handle empty contentsData', () => {
    const { getTooltipStr } = useChartTooltip([]);

    const result = getTooltipStr('Title', []);

    expect(result).toContain('Title');
    expect(result).toContain('<table');
    expect(result).toContain('<tbody>');
  });

  it('should handle multiple tooltip data entries', () => {
    const tooltipData: InspectorAgentChart.MetricValue[] = [
      {
        chartType: 'tooltip',
        fieldName: 'field1',
        unit: 'count',
        valueList: [10, 20, 30],
      },
      {
        chartType: 'tooltip',
        fieldName: 'field2',
        unit: 'count',
        valueList: [40, 50, 60],
      },
      {
        chartType: 'tooltip',
        fieldName: 'field3',
        unit: 'count',
        valueList: [70, 80, 90],
      },
    ];

    const { getTooltipData } = useChartTooltip(tooltipData);

    const result = getTooltipData(2);

    expect(result).toHaveLength(3);
    expect(result[0].id).toBe('field1');
    expect(result[1].id).toBe('field2');
    expect(result[2].id).toBe('field3');
  });
});

