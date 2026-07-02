import React from 'react';
import * as echarts from 'echarts/core';
import { LineChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { SystemMetricMetricInfo } from '@pinpoint-fe/ui/src/constants';
import { useGetSystemMetricChartData, useGetSystemMetricTagsData } from '@pinpoint-fe/ui/src/hooks';
import { getFormat } from '@pinpoint-fe/ui/src/utils';
import { cn } from '../../../lib';
import {
  Card,
  CardHeader,
  CardTitle,
  CardContent,
  CardDescription,
  Label,
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectGroup,
  SelectItem,
  Separator,
} from '../../ui';
import {
  getGridBottom,
  LEGEND_ICON_WIDTH,
  LEGEND_ITEM_GAP,
} from '../../../lib/charts/echartsLegendLayout';
import { useEChartsInstance } from '../../../lib/charts/useEChartsInstance';
import {
  formatAxisTooltip,
  formatCategoryDateLabel,
} from '../../../lib/charts/echartsTimeSeriesFormat';

echarts.use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

// 페이지의 모든 System Metric 차트를 하나의 echarts 그룹으로 묶어 tooltip/axisPointer를 동기화한다.
// (billboard 의 tooltip.linked: true 대체)
const SYSTEM_METRIC_CHART_GROUP = 'system-metric-chart';

export interface SystemMetricChartFetcherProps {
  chartInfo: SystemMetricMetricInfo.MetricInfoData;
  className?: string;
  emptyMessage?: string;
}

export const SystemMetricChartFetcher = ({
  chartInfo,
  className,
  emptyMessage = 'No Data',
}: SystemMetricChartFetcherProps) => {
  const { metricDefinitionId, tagGroup } = chartInfo;
  const { data: tagData } = useGetSystemMetricTagsData({
    metricDefinitionId: tagGroup ? metricDefinitionId : '',
  });
  // 사용자가 직접 고른 tag만 상태로 보관하고, 선택 전에는 tagData의 첫 tag를 파생값으로 사용한다.
  // (useEffect로 뒤늦게 동기화하면 tagData 변경 시 ''로 리셋되는 문제가 있어 렌더 중 파생값으로 처리)
  const [selectedTag, setSelectedTag] = React.useState<string>();
  // 사용자가 고른 tag가 현재 tagData에 없으면(host/metric 전환 등) 첫 tag로 폴백한다.
  const tags = selectedTag && tagData?.includes(selectedTag) ? selectedTag : (tagData?.[0] ?? '');
  const { data: chartData } = useGetSystemMetricChartData({
    metricDefinitionId,
    tagGroup,
    tags,
  });

  const dataUnit = chartData?.metricValueGroups?.[0]?.unit || '';
  const title = chartData?.title || '';

  const { chartRef, chartInstanceRef, renderRef } = useEChartsInstance({
    group: SYSTEM_METRIC_CHART_GROUP,
  });

  React.useEffect(() => {
    if (!chartInstanceRef.current) return;

    const formatValue = getFormat(dataUnit);
    const timestamps = chartData?.timestamp ?? [];
    const metricValues = chartData?.metricValueGroups?.[0]?.metricValues ?? [];
    const hasData =
      timestamps.length > 0 &&
      metricValues.some((mv) => mv.values && mv.values.some((v) => v >= 0));

    const series = metricValues.map(({ fieldName, values }) => ({
      name: fieldName,
      type: 'line' as const,
      data: values.map((v) => (v < 0 ? null : v)),
      showSymbol: false,
      smooth: false,
      lineStyle: {
        width: 1,
      },
      emphasis: {
        focus: 'series' as const,
      },
    }));

    const legendNames = metricValues.map((mv) => mv.fieldName);

    const render = () => {
      const chart = chartInstanceRef.current;
      if (!chart) return;

      const containerWidth = chartRef.current?.clientWidth ?? 0;
      const gridBottom = getGridBottom(legendNames, containerWidth);

      chart.setOption(
        {
          animation: false,
          legend: {
            data: legendNames,
            bottom: 0,
            icon: 'square',
            itemWidth: LEGEND_ICON_WIDTH,
            itemHeight: 10,
            itemGap: LEGEND_ITEM_GAP,
          },
          grid: {
            top: 20,
            bottom: gridBottom,
            right: 25,
            left: 0,
          },
          xAxis: {
            type: 'category',
            data: timestamps,
            axisLabel: {
              show: true,
              formatter: formatCategoryDateLabel,
              showMaxLabel: true,
              showMinLabel: true,
            },
            axisTick: { show: false },
            zlevel: 1,
          },
          yAxis: {
            type: 'value',
            min: 0,
            axisLabel: {
              formatter: formatValue,
            },
            axisLine: {
              show: true,
            },
            axisTick: {
              show: true,
            },
            splitLine: {
              show: false,
            },
            zlevel: 1,
          },
          tooltip: {
            show: true,
            trigger: 'axis',
            confine: true,
            formatter: (params: unknown) => formatAxisTooltip(params, formatValue),
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
        },
        // tag/metric 변경으로 series 수가 줄어도 이전 series가 병합되어 잔존하지 않도록 항상 교체한다.
        { replaceMerge: ['series'] },
      );
    };

    renderRef.current = render;
    render();
  }, [chartData, dataUnit, emptyMessage, chartInstanceRef, chartRef, renderRef]);

  return (
    <Card className="rounded-lg">
      <CardHeader className="px-4 py-3 text-sm">
        <CardTitle>{title}</CardTitle>
        {tagGroup && (
          <CardDescription className="flex items-center gap-2.5 !mt-3">
            <Label className="text-xs">Group</Label>
            <Select value={tags} onValueChange={(value) => setSelectedTag(value)}>
              <SelectTrigger className="w-[calc(100%-3.125rem)] text-xs">
                <span className="flex-1 text-left truncate">
                  <SelectValue>{tags}</SelectValue>
                </span>
              </SelectTrigger>
              <SelectContent className="overflow-auto max-h-72">
                <SelectGroup>
                  {tagData?.map((tag, i) => (
                    <SelectItem className="pr-4 text-xs" key={i} value={tag}>
                      {tag}
                    </SelectItem>
                  ))}
                </SelectGroup>
              </SelectContent>
            </Select>
          </CardDescription>
        )}
      </CardHeader>
      <Separator />
      <CardContent className="p-0 px-3 pb-2">
        <div ref={chartRef} className={cn('w-full aspect-video', className)} />
      </CardContent>
    </Card>
  );
};
