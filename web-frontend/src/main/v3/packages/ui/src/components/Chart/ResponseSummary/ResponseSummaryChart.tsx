import React from 'react';
import { abbreviateNumber, addCommas } from '@pinpoint-fe/ui/src/utils';
import { HelpPopover } from '../../../components/HelpPopover';
import * as echarts from 'echarts/core';
import { BarChart as BarChartEcharts } from 'echarts/charts';
import { AxisBreak } from 'echarts/features';
import { GridComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { cn } from '../../../lib';

echarts.use([BarChartEcharts, AxisBreak, GridComponent, CanvasRenderer]);

export interface ResponseSummaryChartProps {
  /**
   * The order of the data matches the order of the colors.
   */
  data: number[] | ((categories: ResponseSummaryChartProps['categories']) => number[]);
  categories: string[];
  colors?: string[];
  title?: React.ReactNode;
  className?: string;
  emptyMessage?: string;
  disabledBreak?: boolean;
}

type AxisBreakOption = {
  start: number;
  end: number;
  gap?: string;
};

const DEFAULT_CATEGORIES = ['1s', '3s', '5s', 'Slow', 'Error'];
const DEFAULT_COLORS = ['#34b994', '#51afdf', '#ffba00', '#e67f22', '#e95459'];

export const ResponseSummaryChart = ({
  data,
  categories = DEFAULT_CATEGORIES,
  colors = DEFAULT_COLORS,
  className,
  title,
  emptyMessage = 'No Data',
  disabledBreak,
}: ResponseSummaryChartProps) => {
  const chartRef = React.useRef(null);
  const chartInstanceRef = React.useRef<echarts.EChartsType | null>(null);
  const chartData = React.useMemo(
    () => (typeof data === 'function' ? data(categories) : data || []),
    [data, categories],
  );

  // chartData 기반으로 break 설정 계산
  const breakConfig = React.useMemo(() => {
    if (!chartData || chartData.length === 0 || disabledBreak) return [];

    const values = chartData.filter((v) => v > 0);
    if (values.length < 2) return [];

    const uniqueValues = Array.from(new Set(values)).sort((a, b) => a - b);

    const minValue = Math.min(...uniqueValues);
    const GAP_RATIO = 5;
    const MAX_BREAKS = 2;

    // 1. gap 후보 수집
    const candidates = [];

    for (let i = 0; i < uniqueValues.length - 1; i++) {
      const prev = Math.max(uniqueValues[i], 5);
      const next = uniqueValues[i + 1];
      const ratio = next / prev;

      if (ratio >= GAP_RATIO) {
        candidates.push({
          prev,
          next,
          ratio,
        });
      }
    }

    // 2. 차이가 큰 순서대로 정렬
    const sortedCandidates = candidates.sort((a, b) => b.ratio - a.ratio).slice(0, MAX_BREAKS);

    // 3. 최대 2개만 break 생성
    const breakArr: AxisBreakOption[] = [];

    sortedCandidates.forEach(({ prev, next }) => {
      const buffer = Math.min(Math.round(prev * 0.2), minValue); // 각 구간에 맞는 buffer
      const start = prev + buffer;
      const end = next - buffer;

      if (start < end) {
        breakArr.push({
          start,
          end,
          gap: '10%',
        });
      }
    });

    return breakArr;
  }, [chartData, disabledBreak]);

  // 차트 초기화
  React.useEffect(() => {
    if (!chartRef.current) return;

    const chart = echarts.init(chartRef.current);
    chartInstanceRef.current = chart;

    // chart resize
    const wrapperElement = chartRef.current;
    if (!wrapperElement) return;
    const resizeObserver = new ResizeObserver(() => {
      chart.resize();
    });
    resizeObserver.observe(wrapperElement);

    return () => {
      resizeObserver.disconnect();
      chart.dispose();
    };
  }, []);

  // data 변경 시 업데이트
  React.useEffect(() => {
    if (!chartInstanceRef.current) return;

    chartInstanceRef.current.setOption({
      grid: {
        top: 20,
        bottom: 0,
        right: 0,
        left: 0,
      },
      xAxis: {
        type: 'category',
        data: categories,
        axisTick: {
          show: true,
        },
      },
      yAxis: {
        type: 'value',
        min: 0,
        axisLine: {
          show: true,
        },
        showMaxLabel: true,
        axisTick: {
          show: true,
        },
        breaks: breakConfig,
        breakLabelLayout: {
          moveOverlap: true,
        },
        splitNumber: 2,
        axisLabel: {
          formatter: (value: number): string => abbreviateNumber(value, ['', 'K', 'M', 'G']),
        },
        splitLine: {
          lineStyle: {
            type: 'dashed',
          },
        },
      },
      series: [
        {
          data: chartData,
          type: 'bar',
          itemStyle: {
            color: (params: { dataIndex: number }) => {
              return colors[params.dataIndex];
            },
          },
          label: {
            show: true,
            position: 'top',
            offset: [0, 5],
            formatter: (params: { value: number }) => {
              return addCommas(params.value);
            },
            fontSize: 10,
            color: 'inherit',
          },
        },
      ],
      graphic:
        chartData.length === 0
          ? [
              {
                type: 'text',
                left: 'center',
                top: 'middle',
                style: {
                  text: emptyMessage,
                  fontSize: 14,
                  fill: '#999',
                  textAlign: 'center',
                },
              },
            ]
          : [],
    });
  }, [categories, chartData, colors, breakConfig, emptyMessage]);

  return (
    <div className="w-full h-full">
      <div className="flex gap-1">
        {title}
        <HelpPopover helpKey="HELP_VIEWER.RESPONSE_SUMMARY" />
      </div>
      <div className={cn('w-full h-40', className)} ref={chartRef}></div>
    </div>
  );
};
