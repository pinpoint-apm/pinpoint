import { useChartConfig, DEFAULT_CHART_CONFIG } from './useChartConfig';
import { InspectorAgentChart } from '@pinpoint-fe/ui/src/constants';

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

describe('useChartConfig', () => {
  describe('DEFAULT_CHART_CONFIG', () => {
    it('should have correct default values', () => {
      expect(DEFAULT_CHART_CONFIG.PADDING_TOP).toBe(20);
      expect(DEFAULT_CHART_CONFIG.PADDING_BOTTOM).toBe(10);
      expect(DEFAULT_CHART_CONFIG.PADDING_RIGHT).toBe(30);
      expect(DEFAULT_CHART_CONFIG.PADDING_LEFT).toBe(15);
      expect(DEFAULT_CHART_CONFIG.EXTRA_AXIS_PADDING_RIGHT).toBe(40);
      expect(DEFAULT_CHART_CONFIG.DEFAULT_MAX).toBe(10);
      expect(DEFAULT_CHART_CONFIG.X_AXIS_TICK_COUNT).toBe(4);
    });
  });

  describe('useChartConfig', () => {
    it('should return null when data is null', () => {
      const result = useChartConfig(null);

      expect(result).toBeNull();
    });

    it('should return null when data is undefined', () => {
      const result = useChartConfig(undefined);

      expect(result).toBeNull();
    });

    it('should return chartData and chartOptions when data is provided', () => {
      const data: InspectorAgentChart.Response = {
        title: 'Test Chart',
        timestamp: [1000, 2000, 3000],
        metricValues: [
          {
            chartType: 'line',
            fieldName: 'field1',
            unit: 'count',
            valueList: [10, 20, 30],
          },
        ],
      };

      const result = useChartConfig(data);

      expect(result).not.toBeNull();
      expect(result).toHaveProperty('chartData');
      expect(result).toHaveProperty('chartOptions');
    });

    it('should include title and timestamp in chartData', () => {
      const data: InspectorAgentChart.Response = {
        title: 'Test Chart',
        timestamp: [1000, 2000, 3000],
        metricValues: [
          {
            chartType: 'line',
            fieldName: 'field1',
            unit: 'count',
            valueList: [10, 20, 30],
          },
        ],
      };

      const result = useChartConfig(data);

      expect(result?.chartData.title).toBe('Test Chart');
      expect(result?.chartData.timestamp).toEqual([1000, 2000, 3000]);
    });

    it('should configure chart types and axes from metric values', () => {
      const data: InspectorAgentChart.Response = {
        title: 'Test Chart',
        timestamp: [1000, 2000, 3000],
        metricValues: [
          {
            chartType: 'spline',
            fieldName: 'field1',
            unit: 'count',
            valueList: [10, 20, 30],
          },
          {
            chartType: 'bar',
            fieldName: 'field2',
            unit: 'percent',
            valueList: [40, 50, 60],
          },
        ],
      };

      const result = useChartConfig(data);

      expect(result).not.toBeNull();
      if (result && result.chartOptions.data) {
        expect(result.chartOptions.data.types).toBeDefined();
        expect(result.chartOptions.data.axes).toBeDefined();
      }
    });

    it('should use extra padding when multiple axes are present', () => {
      const data: InspectorAgentChart.Response = {
        title: 'Test Chart',
        timestamp: [1000, 2000, 3000],
        metricValues: [
          {
            chartType: 'line',
            fieldName: 'field1',
            unit: 'unit1',
            valueList: [10, 20, 30],
          },
          {
            chartType: 'line',
            fieldName: 'field2',
            unit: 'unit2',
            valueList: [40, 50, 60],
          },
        ],
      };

      const result = useChartConfig(data);

      expect(result).not.toBeNull();
      if (
        result &&
        typeof result.chartOptions.padding === 'object' &&
        result.chartOptions.padding !== null
      ) {
        expect(result.chartOptions.padding.right).toBe(
          DEFAULT_CHART_CONFIG.EXTRA_AXIS_PADDING_RIGHT,
        );
      }
    });

    it('should use default padding when single axis is present', () => {
      const data: InspectorAgentChart.Response = {
        title: 'Test Chart',
        timestamp: [1000, 2000, 3000],
        metricValues: [
          {
            chartType: 'line',
            fieldName: 'field1',
            unit: 'unit1',
            valueList: [10, 20, 30],
          },
        ],
      };

      const result = useChartConfig(data);

      expect(result).not.toBeNull();
      if (
        result &&
        typeof result.chartOptions.padding === 'object' &&
        result.chartOptions.padding !== null
      ) {
        expect(result.chartOptions.padding.right).toBe(DEFAULT_CHART_CONFIG.PADDING_RIGHT);
      }
    });

    it('should include stacking data keys in groups', () => {
      const data: InspectorAgentChart.Response = {
        title: 'Test Chart',
        timestamp: [1000, 2000, 3000],
        metricValues: [
          {
            chartType: 'line',
            fieldName: 'fastCount',
            unit: 'count',
            valueList: [10, 20, 30],
          },
          {
            chartType: 'line',
            fieldName: 'normalCount',
            unit: 'count',
            valueList: [40, 50, 60],
          },
        ],
      };

      const result = useChartConfig(data);

      expect(result).not.toBeNull();
      if (result && result.chartOptions.data) {
        expect(result.chartOptions.data.groups).toBeDefined();
        expect(Array.isArray(result.chartOptions.data.groups)).toBe(true);
      }
    });

    it('should set tooltip linked to false for Apdex Score', () => {
      const data: InspectorAgentChart.Response = {
        title: 'Apdex Score',
        timestamp: [1000, 2000, 3000],
        metricValues: [
          {
            chartType: 'line',
            fieldName: 'field1',
            unit: 'count',
            valueList: [10, 20, 30],
          },
        ],
      };

      const result = useChartConfig(data);

      expect(result?.chartOptions.tooltip?.linked).toBe(false);
    });

    it('should set tooltip linked to true for other charts', () => {
      const data: InspectorAgentChart.Response = {
        title: 'Other Chart',
        timestamp: [1000, 2000, 3000],
        metricValues: [
          {
            chartType: 'line',
            fieldName: 'field1',
            unit: 'count',
            valueList: [10, 20, 30],
          },
        ],
      };

      const result = useChartConfig(data);

      expect(result?.chartOptions.tooltip?.linked).toBe(true);
    });

    it('should merge chartInitOptions dataOptions', () => {
      const data: InspectorAgentChart.Response = {
        title: 'Test Chart',
        timestamp: [1000, 2000, 3000],
        metricValues: [
          {
            chartType: 'line',
            fieldName: 'field1',
            unit: 'count',
            valueList: [10, 20, 30],
          },
        ],
      };

      const chartInitOptions = {
        dataOptions: {
          colors: {
            field1: '#ff0000',
          },
        },
      };

      const result = useChartConfig(data, chartInitOptions);

      expect(result).not.toBeNull();
      if (result && result.chartOptions.data) {
        expect(result.chartOptions.data).toBeDefined();
        expect(result.chartOptions.data.colors).toBeDefined();
        expect(result.chartOptions.data.colors).toMatchObject({
          field1: '#ff0000',
        });
      }
    });

    it('should merge chartInitOptions elseOptions', () => {
      const data: InspectorAgentChart.Response = {
        title: 'Test Chart',
        timestamp: [1000, 2000, 3000],
        metricValues: [
          {
            chartType: 'line',
            fieldName: 'field1',
            unit: 'count',
            valueList: [10, 20, 30],
          },
        ],
      };

      const chartInitOptions = {
        elseOptions: {
          legend: {
            show: false,
          },
        },
      };

      const result = useChartConfig(data, chartInitOptions);

      expect(result?.chartOptions.legend?.show).toBe(false);
    });
  });
});
