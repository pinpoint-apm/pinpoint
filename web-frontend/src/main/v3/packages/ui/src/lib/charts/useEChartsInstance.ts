import React from 'react';
import * as echarts from 'echarts/core';

export interface UseEChartsInstanceOptions {
  // echarts.connect 그룹 id. 지정하면 같은 그룹의 차트끼리 tooltip/axisPointer 등이 동기화된다.
  // (billboard 의 tooltip.linked 를 대체)
  group?: string;
}

// 하단 legend를 쓰는 echarts 차트들이 공유하는 인스턴스 라이프사이클 훅.
// echarts.init + ResizeObserver(resize 후 render 재호출) + dispose 를 담당하고,
// 데이터 effect에서 만든 render 함수를 renderRef에 보관해 resize 시 다시 호출한다.
export const useEChartsInstance = ({ group }: UseEChartsInstanceOptions = {}) => {
  const chartRef = React.useRef<HTMLDivElement>(null);
  const chartInstanceRef = React.useRef<echarts.EChartsType | null>(null);
  const renderRef = React.useRef<(() => void) | null>(null);

  React.useEffect(() => {
    if (!chartRef.current) return;

    const chart = echarts.init(chartRef.current);
    chartInstanceRef.current = chart;
    if (group) {
      chart.group = group;
      echarts.connect(group);
    }

    const wrapperElement = chartRef.current;
    const resizeObserver = new ResizeObserver(() => {
      chart.resize();
      // 폭이 바뀌면 legend 줄바꿈 행 수가 달라지므로 render를 다시 호출해 grid.bottom을 재계산한다.
      renderRef.current?.();
    });
    resizeObserver.observe(wrapperElement);

    return () => {
      resizeObserver.disconnect();
      chart.dispose();
      chartInstanceRef.current = null;
    };
  }, [group]);

  return { chartRef, chartInstanceRef, renderRef };
};
