import { spline, areaSpline, bar, line } from 'billboard.js';

export const useChartType = () => {
  const getChartType = (chartType: string) => {
    switch (chartType) {
      case 'spline':
        return spline();
      case 'areaSpline':
        return areaSpline();
      case 'bar':
        return bar();
      default:
        return line();
    }
  };

  return { getChartType };
};
