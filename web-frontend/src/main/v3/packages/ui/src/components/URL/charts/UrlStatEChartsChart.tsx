import React from 'react';
import * as echarts from 'echarts/core';
import { BarChart as BarChartEcharts, LineChart as LineChartEcharts } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { formatNewLinedDateString } from '@pinpoint-fe/ui/src/utils';
import { isValid } from 'date-fns';
import { UrlStatChartType as UrlStatChartApi } from '@pinpoint-fe/ui/src/constants';
import { cn } from '../../../lib';

echarts.use([
  BarChartEcharts,
  LineChartEcharts,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  CanvasRenderer,
]);

export interface UrlStatEChartsChartProps {
  chartType: 'bar' | 'line';
  yAxisName: 'Total Count' | 'Failure Count' | 'Apdex Score' | 'Latency (ms)';
  chartColors: string[];
  formatter?: (value: number) => string;
  data: UrlStatChartApi.Response | null;
  className?: string;
  emptyMessage?: string;
}

export const UrlStatEChartsChart = ({
  chartType,
  yAxisName,
  chartColors,
  formatter,
  data,
  className,
  emptyMessage = 'No Data',
}: UrlStatEChartsChartProps) => {
  const chartRef = React.useRef<HTMLDivElement>(null);
  const chartInstanceRef = React.useRef<echarts.EChartsType | null>(null);

  React.useEffect(() => {
    if (!chartRef.current) return;

    const chart = echarts.init(chartRef.current);
    chartInstanceRef.current = chart;

    const wrapperElement = chartRef.current;
    const resizeObserver = new ResizeObserver(() => {
      chart.resize();
    });
    resizeObserver.observe(wrapperElement);

    return () => {
      resizeObserver.disconnect();
      chart.dispose();
      chartInstanceRef.current = null;
    };
  }, []);

  React.useEffect(() => {
    if (!chartInstanceRef.current) return;

    const hasData =
      data &&
      data.timestamp.length > 0 &&
      data.metricValueGroups[0]?.metricValues?.some(
        (mv) => mv.values && mv.values.some((v) => v >= 0),
      );

    const timestamps = data?.timestamp ?? [];
    const metricValues = data?.metricValueGroups[0]?.metricValues ?? [];

    const series = metricValues.map(({ fieldName, values }, seriesIndex) => {
      if (chartType === 'line') {
        return {
          name: fieldName,
          type: 'line' as const,
          data: values.map((v) => (v < 0 ? null : v)),
          showSymbol: false,
          smooth: false,
          lineStyle: {
            width: 1,
          },
          itemStyle: {
            color: chartColors[seriesIndex] ?? chartColors[0],
          },
          emphasis: {
            focus: 'series',
          },
        };
      }

      return {
        name: fieldName,
        type: 'bar' as const,
        stack: 'total',
        data: values.map((v) => (v < 0 ? null : v)),
        itemStyle: {
          color: chartColors[seriesIndex],
        },
      };
    });

    chartInstanceRef.current.setOption({
      animation: false,
      legend: {
        data: metricValues.map((mv) => mv.fieldName),
        bottom: 0,
        icon: 'square',
        itemWidth: 10,
        itemHeight: 10,
        itemGap: 15,
      },
      grid: {
        top: 20,
        bottom: 60,
        right: 30,
        left: 0,
      },
      xAxis: {
        type: 'category',
        data: timestamps,
        axisLabel: {
          show: true,
          formatter: (value: number | string) => {
            const ts = typeof value === 'string' ? Number(value) : value;
            const date = new Date(ts);
            if (isValid(date)) {
              return formatNewLinedDateString(date);
            }
            return String(value);
          },
          showMaxLabel: true,
          showMinLabel: true,
        },
        axisTick: { show: false },
        zlevel: 1,
      },
      yAxis: {
        type: 'value',
        name: yAxisName,
        nameGap: 40,
        nameLocation: 'middle',
        min: 0,
        axisLabel: {
          formatter: formatter ?? ((value: number) => String(value)),
        },
        axisLine: {
          show: true,
        },
        axisTick: {
          show: true,
        },
        splitLine: {
          show: true,
          lineStyle: { type: 'dashed' },
        },
        zlevel: 1,
      },
      tooltip: {
        show: true,
        trigger: 'axis',
        confine: true,
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        formatter: (params: any) => {
          if (!Array.isArray(params) || params.length === 0) return '';
          const firstParam = params[0];
          const axisValue = firstParam.axisValue;
          const axisValueNum = typeof axisValue === 'number' ? axisValue : Number(axisValue);
          const dateStr = isValid(new Date(axisValueNum))
            ? formatNewLinedDateString(axisValueNum).replace('\n', ' ')
            : String(axisValue);
          const rows = params
            .map(
              (param: {
                value?: number | [number, number];
                seriesName?: string;
                color?: string;
              }) => {
                const yValue = typeof param.value === 'number' ? param.value : param.value?.[1];
                if (yValue == null) return null;
                return `<div style="display: flex; justify-content: space-between; gap: 12px; align-items: center;">
                  <div style="display: flex; gap: 5px; align-items: center;">
                    <div style="width: 10px; height: 10px; background: ${param.color};"></div>${param.seriesName}
                  </div>
                  <div>${formatter ? formatter(yValue) : String(yValue)}</div>
                </div>`;
              },
            )
            .filter(Boolean)
            .join('');
          return `<div>
            <div style="margin-bottom: 5px;"><strong>${dateStr}</strong></div>
            ${rows}
          </div>`;
        },
      },
      series,
      graphic: !hasData
        ? [
            {
              type: 'text',
              left: 'center',
              top: 'middle',
              style: {
                text: emptyMessage,
                fontSize: 18,
                fill: '#999',
                textAlign: 'center',
              },
            },
          ]
        : [],
    });
  }, [data, emptyMessage, chartType, yAxisName, chartColors, formatter]);

  return <div className={cn('w-full h-full', className)} ref={chartRef} />;
};
