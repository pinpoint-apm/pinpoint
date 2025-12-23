import { useDataSourceChartConfig } from './useDataSourceChartConfig';
import { InspectorAgentDataSourceChart } from '@pinpoint-fe/ui/src/constants';

// Mock billboard.js
jest.mock('billboard.js', () => ({
  spline: jest.fn(() => ({ type: 'spline' })),
  areaSpline: jest.fn(() => ({ type: 'areaSpline' })),
  bar: jest.fn(() => ({ type: 'bar' })),
  line: jest.fn(() => ({ type: 'line' })),
}));

// Mock dependencies
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

// Mock document.querySelector for tooltip
const mockQuerySelector = jest.fn(() => ({
  innerHTML: '',
}));

beforeEach(() => {
  document.querySelector = mockQuerySelector;
});

describe('useDataSourceChartConfig', () => {
  it('should return null when data is null', () => {
    const result = useDataSourceChartConfig(null);

    expect(result).toBeNull();
  });

  it('should return null when data is undefined', () => {
    const result = useDataSourceChartConfig(undefined);

    expect(result).toBeNull();
  });

  it('should return chartData and chartOptions when data is provided', () => {
    const data: InspectorAgentDataSourceChart.Response = {
      title: 'Test Chart',
      timestamp: [1000, 2000, 3000],
      metricValueGroups: [
        {
          metricValues: [
            {
              chartType: 'line',
              fieldName: 'activeAvg',
              unit: 'count',
              valueList: [10, 20, 30],
            },
          ],
          tags: [
            { name: 'id', value: 'id1' },
            { name: 'databaseName', value: 'Database1' },
          ],
        },
      ],
    };

    const result = useDataSourceChartConfig(data);

    expect(result).not.toBeNull();
    expect(result).toHaveProperty('chartData');
    expect(result).toHaveProperty('chartOptions');
  });

  it('should include title and timestamp in chartData', () => {
    const data: InspectorAgentDataSourceChart.Response = {
      title: 'Test Chart',
      timestamp: [1000, 2000, 3000],
      metricValueGroups: [
        {
          metricValues: [
            {
              chartType: 'line',
              fieldName: 'activeAvg',
              unit: 'count',
              valueList: [10, 20, 30],
            },
          ],
          tags: [
            { name: 'id', value: 'id1' },
            { name: 'databaseName', value: 'Database1' },
          ],
        },
      ],
    };

    const result = useDataSourceChartConfig(data);

    expect(result?.chartData.title).toBe('Test Chart');
    expect(result?.chartData.timestamp).toEqual([1000, 2000, 3000]);
  });

  it('should create fieldName from databaseName and id', () => {
    const data: InspectorAgentDataSourceChart.Response = {
      title: 'Test Chart',
      timestamp: [1000, 2000, 3000],
      metricValueGroups: [
        {
          metricValues: [
            {
              chartType: 'line',
              fieldName: 'activeAvg',
              unit: 'count',
              valueList: [10, 20, 30],
            },
          ],
          tags: [
            { name: 'id', value: 'id1' },
            { name: 'databaseName', value: 'Database1' },
          ],
        },
      ],
    };

    const result = useDataSourceChartConfig(data);

    expect(result?.chartData.metricValues[0].fieldName).toBe('Database1-id1');
    expect(result?.chartData.metricValues[0].dataLabel).toBe('Database1');
  });

  it('should use index as id when id tag is missing', () => {
    const data: InspectorAgentDataSourceChart.Response = {
      title: 'Test Chart',
      timestamp: [1000, 2000, 3000],
      metricValueGroups: [
        {
          metricValues: [
            {
              chartType: 'line',
              fieldName: 'activeAvg',
              unit: 'count',
              valueList: [10, 20, 30],
            },
          ],
          tags: [{ name: 'databaseName', value: 'Database1' }],
        },
      ],
    };

    const result = useDataSourceChartConfig(data);

    expect(result?.chartData.metricValues[0].fieldName).toBe('Database1-0');
  });

  it('should use "Database" as default databaseName when missing', () => {
    const data: InspectorAgentDataSourceChart.Response = {
      title: 'Test Chart',
      timestamp: [1000, 2000, 3000],
      metricValueGroups: [
        {
          metricValues: [
            {
              chartType: 'line',
              fieldName: 'activeAvg',
              unit: 'count',
              valueList: [10, 20, 30],
            },
          ],
          tags: [{ name: 'id', value: 'id1' }],
        },
      ],
    };

    const result = useDataSourceChartConfig(data);

    expect(result?.chartData.metricValues[0].fieldName).toBe('Database-id1');
    expect(result?.chartData.metricValues[0].dataLabel).toBe('Database');
  });

  it('should configure chart types, axes, and names', () => {
    const data: InspectorAgentDataSourceChart.Response = {
      title: 'Test Chart',
      timestamp: [1000, 2000, 3000],
      metricValueGroups: [
        {
          metricValues: [
            {
              chartType: 'spline',
              fieldName: 'activeAvg',
              unit: 'count',
              valueList: [10, 20, 30],
            },
          ],
          tags: [
            { name: 'id', value: 'id1' },
            { name: 'databaseName', value: 'Database1' },
          ],
        },
      ],
    };

    const result = useDataSourceChartConfig(data);

    expect(result?.chartOptions.data.types).toBeDefined();
    expect(result?.chartOptions.data.axes).toBeDefined();
    expect(result?.chartOptions.data.names).toBeDefined();
  });

  it('should set legend show to false', () => {
    const data: InspectorAgentDataSourceChart.Response = {
      title: 'Test Chart',
      timestamp: [1000, 2000, 3000],
      metricValueGroups: [
        {
          metricValues: [
            {
              chartType: 'line',
              fieldName: 'activeAvg',
              unit: 'count',
              valueList: [10, 20, 30],
            },
          ],
          tags: [
            { name: 'id', value: 'id1' },
            { name: 'databaseName', value: 'Database1' },
          ],
        },
      ],
    };

    const result = useDataSourceChartConfig(data);

    expect(result?.chartOptions.legend?.show).toBe(false);
  });

  it('should configure axis with label and format', () => {
    const data: InspectorAgentDataSourceChart.Response = {
      title: 'Test Chart',
      timestamp: [1000, 2000, 3000],
      metricValueGroups: [
        {
          metricValues: [
            {
              chartType: 'line',
              fieldName: 'activeAvg',
              unit: 'count',
              valueList: [10, 20, 30],
            },
          ],
          tags: [
            { name: 'id', value: 'id1' },
            { name: 'databaseName', value: 'Database1' },
          ],
        },
      ],
    };

    const result = useDataSourceChartConfig(data);

    const axisKeys = Object.keys(result?.chartOptions.axis || {});
    expect(axisKeys.length).toBeGreaterThan(0);
    
    const firstAxis = result?.chartOptions.axis?.[axisKeys[0]];
    expect(firstAxis?.label?.text).toBe('Active Avg');
    expect(firstAxis?.label?.position).toBe('outer-middle');
    expect(firstAxis?.show).toBe(true);
  });

  it('should handle multiple metric value groups', () => {
    const data: InspectorAgentDataSourceChart.Response = {
      title: 'Test Chart',
      timestamp: [1000, 2000, 3000],
      metricValueGroups: [
        {
          metricValues: [
            {
              chartType: 'line',
              fieldName: 'activeAvg',
              unit: 'count',
              valueList: [10, 20, 30],
            },
          ],
          tags: [
            { name: 'id', value: 'id1' },
            { name: 'databaseName', value: 'Database1' },
          ],
        },
        {
          metricValues: [
            {
              chartType: 'line',
              fieldName: 'activeAvg',
              unit: 'count',
              valueList: [40, 50, 60],
            },
          ],
          tags: [
            { name: 'id', value: 'id2' },
            { name: 'databaseName', value: 'Database2' },
          ],
        },
      ],
    };

    const result = useDataSourceChartConfig(data);

    expect(result?.chartData.metricValues).toHaveLength(2);
    expect(result?.chartData.metricValues[0].fieldName).toBe('Database1-id1');
    expect(result?.chartData.metricValues[1].fieldName).toBe('Database2-id2');
  });

  it('should configure tooltip contents function', () => {
    const data: InspectorAgentDataSourceChart.Response = {
      title: 'Test Chart',
      timestamp: [1000, 2000, 3000],
      metricValueGroups: [
        {
          metricValues: [
            {
              chartType: 'line',
              fieldName: 'activeAvg',
              unit: 'count',
              valueList: [10, 20, 30],
            },
          ],
          tags: [
            { name: 'id', value: 'id1' },
            { name: 'databaseName', value: 'Database1' },
          ],
        },
      ],
    };

    const result = useDataSourceChartConfig(data);

    expect(result?.chartOptions.tooltip?.contents).toBeDefined();
    expect(typeof result?.chartOptions.tooltip?.contents).toBe('function');
  });
});

