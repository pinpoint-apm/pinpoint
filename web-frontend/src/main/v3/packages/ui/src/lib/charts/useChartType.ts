// 서버가 내려주는 chartType 문자열을 ECharts 시리즈 렌더링 옵션으로 변환한다.
// (billboard 의 spline/areaSpline/bar/line 을 대체)
// - spline: 부드러운 라인
// - areaSpline: 부드러운 영역(area) 라인
// - bar: 막대
// - 그 외: 직선 라인
export interface EChartsSeriesTypeOption {
  type: 'line' | 'bar';
  area: boolean;
  smooth: boolean;
}

export const useChartType = () => {
  const getChartType = (chartType: string): EChartsSeriesTypeOption => {
    switch (chartType) {
      case 'spline':
        return { type: 'line', area: false, smooth: true };
      case 'areaSpline':
        return { type: 'line', area: true, smooth: true };
      case 'bar':
        return { type: 'bar', area: false, smooth: false };
      default:
        return { type: 'line', area: false, smooth: false };
    }
  };

  return { getChartType };
};
