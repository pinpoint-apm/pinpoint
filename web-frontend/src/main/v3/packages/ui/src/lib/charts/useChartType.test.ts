import { useChartType } from './useChartType';

describe('useChartType', () => {
  it('should return getChartType function', () => {
    const { getChartType } = useChartType();

    expect(typeof getChartType).toBe('function');
  });

  it('should return a smooth line (no area) for "spline"', () => {
    const { getChartType } = useChartType();

    expect(getChartType('spline')).toEqual({ type: 'line', area: false, smooth: true });
  });

  it('should return a smooth area line for "areaSpline"', () => {
    const { getChartType } = useChartType();

    expect(getChartType('areaSpline')).toEqual({ type: 'line', area: true, smooth: true });
  });

  it('should return a bar for "bar"', () => {
    const { getChartType } = useChartType();

    expect(getChartType('bar')).toEqual({ type: 'bar', area: false, smooth: false });
  });

  it('should return a straight line for "line"', () => {
    const { getChartType } = useChartType();

    expect(getChartType('line')).toEqual({ type: 'line', area: false, smooth: false });
  });

  it('should return a straight line as default for unknown type', () => {
    const { getChartType } = useChartType();

    expect(getChartType('unknown')).toEqual({ type: 'line', area: false, smooth: false });
  });

  it('should return a straight line for empty string', () => {
    const { getChartType } = useChartType();

    expect(getChartType('')).toEqual({ type: 'line', area: false, smooth: false });
  });
});
