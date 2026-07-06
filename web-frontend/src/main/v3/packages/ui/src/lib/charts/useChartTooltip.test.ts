import {
  AxisTooltipParam,
  createChartTooltipFormatter,
  TOOLTIP_NOT_LINKED_CHART_LIST,
} from './useChartTooltip';
import { InspectorAgentChart } from '@pinpoint-fe/ui/src/constants';

describe('TOOLTIP_NOT_LINKED_CHART_LIST', () => {
  it('should contain Apdex Score', () => {
    expect(TOOLTIP_NOT_LINKED_CHART_LIST).toContain('Apdex Score');
  });
});

describe('createChartTooltipFormatter', () => {
  const params: AxisTooltipParam[] = [
    { axisValue: 1000, dataIndex: 0, seriesName: 'field1', color: '#ff0000', value: 100 },
    { axisValue: 1000, dataIndex: 0, seriesName: 'field2', color: '#00ff00', value: 200 },
  ];

  it('should return an empty string when params is not an array or is empty', () => {
    const formatter = createChartTooltipFormatter({ unitByField: {}, tooltipData: [] });

    expect(formatter([])).toBe('');
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    expect(formatter(undefined as any)).toBe('');
  });

  it('should render a row per series with its formatted value', () => {
    const formatter = createChartTooltipFormatter({
      unitByField: { field1: 'count', field2: 'count' },
      tooltipData: [],
    });

    const result = formatter(params);

    expect(result).toContain('field1');
    expect(result).toContain('field2');
    expect(result).toContain('#ff0000');
    expect(result).toContain('#00ff00');
    expect(result).toContain('100');
    expect(result).toContain('200');
  });

  it('should skip series whose value is null', () => {
    const formatter = createChartTooltipFormatter({
      unitByField: { field1: 'count', field2: 'count' },
      tooltipData: [],
    });

    const result = formatter([
      { axisValue: 1000, dataIndex: 0, seriesName: 'field1', color: '#ff0000', value: null },
      params[1],
    ]);

    expect(result).not.toContain('field1');
    expect(result).toContain('field2');
  });

  it('should append tooltip-only rows at the focused index', () => {
    const tooltipData: InspectorAgentChart.MetricValue[] = [
      { chartType: 'tooltip', fieldName: 'extra', unit: 'count', valueList: [10, 20, 30] },
    ];
    const formatter = createChartTooltipFormatter({
      unitByField: { field1: 'count' },
      tooltipData,
    });

    const result = formatter([
      { axisValue: 3000, dataIndex: 2, seriesName: 'field1', color: '#ff0000', value: 5 },
    ]);

    expect(result).toContain('extra');
    // valueList[2] === 30, formatted by the count formatter
    expect(result).toContain('30');
  });

  it('should escape HTML in series names to prevent injection', () => {
    const formatter = createChartTooltipFormatter({
      unitByField: { '<img src=x>': 'count' },
      tooltipData: [],
    });

    const result = formatter([
      { axisValue: 1000, dataIndex: 0, seriesName: '<img src=x>', color: '#ff0000', value: 1 },
    ]);

    expect(result).not.toContain('<img src=x>');
    expect(result).toContain('&lt;img');
  });
});
