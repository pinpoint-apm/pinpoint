import React from 'react';
import * as echarts from 'echarts/core';
import { BarChart, LineChart } from 'echarts/charts';
import { GridComponent, MarkLineComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { Chart } from '@pinpoint-fe/ui/src/constants';
import { CHART_SERIES_COLORS, getRandomColorInHSL } from '../../lib/colors';
import { useEChartsInstance } from '../../lib/charts/useEChartsInstance';

echarts.use([BarChart, LineChart, GridComponent, MarkLineComponent, CanvasRenderer]);

export interface MiniChartProps {
  chart: Chart;
}

export const MiniChart = ({ chart }: MiniChartProps) => {
  const { chartRef, chartInstanceRef } = useEChartsInstance();

  React.useEffect(() => {
    if (!chartInstanceRef.current) return;

    const timestamps = chart?.timestamp ?? [];

    // metricValueGroup 안의 metricValue 하나가 시리즈 하나가 된다. (색상은 그룹 내 index 기준)
    const entries = (chart?.metricValueGroups ?? []).flatMap((mvg) =>
      (mvg?.metricValues ?? []).map((mv, mvi) => ({ mv, mvi, chartType: mvg?.chartType })),
    );

    // -1 은 "값 없음"이므로 null 로 치환하고, 최대값은 기준선(markLine)에 쓴다.
    let maxValue = -1;
    entries.forEach(({ mv }) => {
      mv?.values?.forEach((raw) => {
        if (raw !== -1 && raw != null && raw > maxValue) {
          maxValue = raw;
        }
      });
    });

    const hasData = timestamps.length > 0 && entries.length > 0;
    const showMarkLine = hasData && maxValue > -1;

    const series = entries.map(({ mv, mvi, chartType }, index) => {
      const data = timestamps.map((t, i) => {
        const raw = mv?.values?.[i];
        return [t, raw === -1 || raw == null ? null : raw];
      });
      const color = CHART_SERIES_COLORS[mvi] ?? getRandomColorInHSL();

      return {
        name: mv?.fieldName || '',
        type: chartType === 'bar' ? ('bar' as const) : ('line' as const),
        data,
        showSymbol: false,
        smooth: chartType !== 'bar',
        lineStyle: { width: 1 },
        areaStyle: chartType === 'area' ? { opacity: 0.4 } : undefined,
        itemStyle: { color },
        // 기준선(최대값)은 첫 시리즈에만 붙여 라벨 중복을 막는다.
        ...(index === 0 && showMarkLine
          ? {
              markLine: {
                silent: true,
                symbol: 'none',
                lineStyle: { color: 'black', type: 'dashed' as const, width: 1 },
                label: {
                  position: 'end' as const,
                  fontSize: 10,
                  formatter: () => String(Math.round(maxValue * 100) / 100),
                },
                data: [{ yAxis: maxValue }],
              },
            }
          : {}),
      };
    });

    const render = () => {
      const chartInstance = chartInstanceRef.current;
      if (!chartInstance) return;

      chartInstance.setOption(
        {
          grid: { top: 6, right: 60, bottom: 6, left: 2 },
          xAxis: {
            type: 'time',
            show: false,
            boundaryGap: false,
          },
          yAxis: {
            type: 'value',
            show: false,
            scale: true,
          },
          series,
        },
        { notMerge: true },
      );
    };

    // MiniChart는 legend가 없고 grid가 고정이라 폭 변화 시 재계산할 게 없다.
    // 따라서 renderRef에 등록하지 않아 resize 때는 chart.resize()만 돌고,
    // setOption 전체 재호출(불필요한 작업)은 건너뛴다.
    render();
  }, [chart, chartInstanceRef]);

  return <div ref={chartRef} className="w-full h-[40px]" />;
};
