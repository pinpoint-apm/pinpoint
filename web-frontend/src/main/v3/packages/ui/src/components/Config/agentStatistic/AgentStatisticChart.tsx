import React from 'react';
import * as echarts from 'echarts/core';
import { BarChart } from 'echarts/charts';
import { GridComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { colors } from '@pinpoint-fe/ui/src/constants';
import { useEChartsInstance } from '../../../lib/charts/useEChartsInstance';
import { buildBottomLegend } from '../../../lib/charts/echartsLegendLayout';

echarts.use([BarChart, GridComponent, LegendComponent, CanvasRenderer]);

export type ChartData = {
  vmVersion?: string;
  agentVersion?: string;
  value: number;
};

export function AgentStatisticChart({
  type,
  chartData,
}: {
  type: 'vmVersion' | 'agentVersion';
  chartData?: ChartData[];
}) {
  const { chartRef, chartInstanceRef, renderRef } = useEChartsInstance();
  const seriesName = type === 'vmVersion' ? 'JVM' : 'Agent';
  const color = type === 'vmVersion' ? colors.blue[600] : colors.orange[500];

  React.useEffect(() => {
    if (!chartInstanceRef.current) return;

    // 카테고리(버전) 하나가 막대 하나. recharts YAxis(type=category) 렌더 순서와 맞추기 위해
    // 첫 항목을 위에 두도록 yAxis.inverse 를 켠다.
    const categories = (chartData ?? []).map((d) => d[type] ?? '');
    const values = (chartData ?? []).map((d) => d.value);

    const render = () => {
      const chart = chartInstanceRef.current;
      if (!chart) return;

      chart.setOption(
        {
          animation: false,
          legend: buildBottomLegend([seriesName]),
          // 버전 문자열 길이가 type 별로 다르므로 containLabel 로 좌측 라벨 폭을 자동 확보한다.
          // (recharts 의 YAxis width 60/120 를 대체)
          grid: { top: 10, right: 50, bottom: 28, left: 5, containLabel: true },
          xAxis: {
            type: 'value',
            minInterval: 1,
          },
          yAxis: {
            type: 'category',
            inverse: true,
            data: categories,
            axisTick: { show: false },
          },
          series: [
            {
              name: seriesName,
              type: 'bar' as const,
              data: values,
              itemStyle: { color },
              // 막대 끝에 값 라벨 표시 (recharts LabelList position="right" 대체)
              label: { show: true, position: 'right' as const, fontSize: 12 },
            },
          ],
        },
        { replaceMerge: ['series', 'yAxis'] },
      );
    };

    renderRef.current = render;
    render();
  }, [chartData, type, seriesName, color, chartInstanceRef, renderRef]);

  // echarts 는 div 안에 고정 크기 canvas 를 그린다. flex 항목에서 canvas 가 min-content 로 작용해
  // 컨테이너가 축소되지 못하는 것을 막기 위해 min-w-0/min-h-0 + overflow-hidden 을 준다.
  // (ChartCore 와 동일한 처리) 이래야 축소 시 ResizeObserver 가 동작해 chart.resize 가 호출된다.
  return <div ref={chartRef} className="w-full h-full min-w-0 min-h-0 overflow-hidden" />;
}
