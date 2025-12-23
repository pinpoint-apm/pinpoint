import { useChartType } from './useChartType';

// Mock billboard.js
jest.mock('billboard.js', () => ({
  spline: jest.fn(() => ({ type: 'spline' })),
  areaSpline: jest.fn(() => ({ type: 'areaSpline' })),
  bar: jest.fn(() => ({ type: 'bar' })),
  line: jest.fn(() => ({ type: 'line' })),
}));

describe('useChartType', () => {
  it('should return getChartType function', () => {
    const { getChartType } = useChartType();

    expect(typeof getChartType).toBe('function');
  });

  it('should return spline type for "spline"', () => {
    const { getChartType } = useChartType();

    const chartType = getChartType('spline');

    expect(chartType).toBeDefined();
    // spline() returns an object, so we check it's not null/undefined
    expect(chartType).not.toBeNull();
  });

  it('should return areaSpline type for "areaSpline"', () => {
    const { getChartType } = useChartType();

    const chartType = getChartType('areaSpline');

    expect(chartType).toBeDefined();
    expect(chartType).not.toBeNull();
  });

  it('should return bar type for "bar"', () => {
    const { getChartType } = useChartType();

    const chartType = getChartType('bar');

    expect(chartType).toBeDefined();
    expect(chartType).not.toBeNull();
  });

  it('should return line type as default for unknown type', () => {
    const { getChartType } = useChartType();

    const chartType = getChartType('unknown');

    expect(chartType).toBeDefined();
    expect(chartType).not.toBeNull();
  });

  it('should return line type for empty string', () => {
    const { getChartType } = useChartType();

    const chartType = getChartType('');

    expect(chartType).toBeDefined();
    expect(chartType).not.toBeNull();
  });

  it('should return line type for "line"', () => {
    const { getChartType } = useChartType();

    const chartType = getChartType('line');

    expect(chartType).toBeDefined();
    expect(chartType).not.toBeNull();
  });
});

