import { useChartAxis } from './useChartAxis';

describe('useChartAxis', () => {
  it('should return chartUnitAxisInfo and getDataAxis function', () => {
    const { chartUnitAxisInfo, getDataAxis } = useChartAxis();

    expect(chartUnitAxisInfo).toBeDefined();
    expect(typeof getDataAxis).toBe('function');
  });

  it('should assign "y" axis for the first unit', () => {
    const { getDataAxis } = useChartAxis();

    const axis = getDataAxis('unit1');

    expect(axis).toBe('y');
  });

  it('should assign "y2" axis for the second unit', () => {
    const { getDataAxis } = useChartAxis();

    getDataAxis('unit1');
    const axis = getDataAxis('unit2');

    expect(axis).toBe('y2');
  });

  it('should assign "y3" axis for the third unit', () => {
    const { getDataAxis } = useChartAxis();

    getDataAxis('unit1');
    getDataAxis('unit2');
    const axis = getDataAxis('unit3');

    expect(axis).toBe('y3');
  });

  it('should return the same axis for the same unit', () => {
    const { getDataAxis } = useChartAxis();

    const axis1 = getDataAxis('unit1');
    const axis2 = getDataAxis('unit1');

    expect(axis1).toBe(axis2);
    expect(axis1).toBe('y');
  });

  it('should maintain separate axis mappings for different units', () => {
    const { getDataAxis, chartUnitAxisInfo } = useChartAxis();

    getDataAxis('unit1');
    getDataAxis('unit2');
    getDataAxis('unit3');

    expect(chartUnitAxisInfo['unit1']).toBe('y');
    expect(chartUnitAxisInfo['unit2']).toBe('y2');
    expect(chartUnitAxisInfo['unit3']).toBe('y3');
  });

  it('should handle multiple calls with different units', () => {
    const { getDataAxis } = useChartAxis();

    const axes = ['unit1', 'unit2', 'unit3', 'unit4'].map((unit) => getDataAxis(unit));

    expect(axes).toEqual(['y', 'y2', 'y3', 'y4']);
  });
});
