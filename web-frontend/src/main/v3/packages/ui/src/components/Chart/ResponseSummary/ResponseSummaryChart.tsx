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
}

export const ResponseSummaryChart = ({
  data,
  categories = ['1s', '3s', '5s', 'Slow', 'Error'],
  colors = ['#34b994', '#51afdf', '#ffba00', '#e67f22', '#e95459'],
  className,
  title,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  emptyMessage = 'No Data',
}: ResponseSummaryChartProps) => {
  const chartRef = React.useRef(null);
  const chartInstanceRef = React.useRef<echarts.EChartsType | null>(null);
  const chartData = React.useMemo(
    () => (typeof data === 'function' ? data(categories) : data || []),
    [data, categories],
  );

  // chartData 기반으로 break 설정 계산
  const breakConfig = React.useMemo(() => {
    if (!chartData || chartData.length === 0) return [];

    const nonZeroValues = chartData.filter((v) => v > 0);
    if (nonZeroValues.length === 0) return [];

    const uniqueValues = Array.from(new Set(nonZeroValues)).sort((a, b) => a - b); // 중복 제거 및 정렬
    if (uniqueValues.length < 2) return [];

    const minValue = Math.min(...uniqueValues);
    const buffer = Math.ceil(minValue / 10) * 10;

    const breaks = [];
    const GAP_RATIO = 5; // 인접한 값들 사이의 비율이 이 값보다 크면 break 생성

    for (let i = 0; i < uniqueValues.length - 1; i++) {
      const currentValue = uniqueValues[i];
      const nextValue = uniqueValues[i + 1];
      const ratio = nextValue / currentValue;

      // 비율이 임계값보다 크면 break 생성
      if (ratio >= GAP_RATIO) {
        const breakStart = currentValue + buffer;
        const breakEnd = nextValue - buffer;

        if (breakStart < breakEnd) {
          breaks.push({
            start: breakStart,
            end: breakEnd,
            gap: '5%',
          });
        }
      }
    }

    return breaks;
  }, [chartData]);

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
  }, [categories, chartData, colors, breakConfig]);

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
