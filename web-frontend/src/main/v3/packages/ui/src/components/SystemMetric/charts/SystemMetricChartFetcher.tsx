import 'billboard.js/dist/billboard.css';
import React from 'react';
import { SystemMetricMetricInfo } from '@pinpoint-fe/ui/src/constants';
import { useGetSystemMetricChartData, useGetSystemMetricTagsData } from '@pinpoint-fe/ui/src/hooks';
import bb, { ChartOptions, line, canvas } from 'billboard.js/canvas';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import BillboardJS, { IChart } from '@billboard.js/react';
import { isValid } from 'date-fns';
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
import { formatNewLinedDateString, getFormat } from '@pinpoint-fe/ui/src/utils';

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

  const chartComponent = React.useRef<IChart>(null);
  const containerRef = React.useRef<HTMLDivElement>(null);
  // canvas 모드는 차트 높이를 스스로 측정해 캔버스 크기로 쓰는데, Group 변경 시 suspense 리마운트 순간
  // 높이가 0으로 측정되면 billboard 기본값(320px)으로 그려져 박스보다 커지며 잘린다. 컨테이너 높이를
  // 직접 측정해 size.height로 넘기면 billboard가 측정/폴백 없이 그 높이로 그리고, 리사이즈에도 대응한다.
  const [measuredHeight, setMeasuredHeight] = React.useState<number>();
  React.useLayoutEffect(() => {
    const container = containerRef.current;
    if (!container) return;
    const updateHeight = () => {
      const height = container.clientHeight;
      if (height > 0) {
        setMeasuredHeight(height);
        chartComponent.current?.instance?.resize({ height });
      }
    };
    updateHeight();
    const resizeObserver = new ResizeObserver(updateHeight);
    resizeObserver.observe(container);
    return () => resizeObserver.disconnect();
  }, []);
  const options: ChartOptions = {
    // v4 ESM: canvas 렌더링 모드 사용.
    render: {
      mode: canvas(),
    },
    ...(measuredHeight ? { size: { height: measuredHeight } } : {}),
    data: {
      x: 'dates',
      columns: [],
      empty: {
        label: {
          text: emptyMessage,
        },
      },
      type: line(),
    },
    padding: {
      mode: 'fit',
      top: 20,
      bottom: 10,
      right: 25,
      left: 15,
    },
    axis: {
      x: {
        type: 'timeseries',
        tick: {
          count: 4,
          format: (date: Date) => {
            if (isValid(date)) {
              return `${formatNewLinedDateString(date)}`;
            }
            return '';
          },
        },
      },
      y: {
        tick: {
          format: getFormat(dataUnit),
        },
        padding: {
          bottom: 0,
        },
        min: 0,
        default: [0, 10],
      },
    },
    point: {
      r: 0,
      focus: {
        only: true,
        expand: {
          r: 3,
        },
      },
    },
    resize: {
      auto: 'parent',
      timer: false,
    },
    transition: {
      duration: 0,
    },
    tooltip: {
      linked: true,
      order: '',
      format: {
        value: getFormat(dataUnit),
      },
    },
  };

  React.useEffect(() => {
    const chart = chartComponent.current?.instance;

    chart?.load({
      columns: chartData
        ? [
            ['dates', ...chartData.timestamp],
            ...(chartData.metricValueGroups?.[0]?.metricValues ?? []).map(
              ({ fieldName, values }) => {
                return [fieldName, ...values.map((v: number) => (v < 0 ? null : v))];
              },
            ),
          ]
        : [],
      resizeAfter: true,
    });
  }, [chartData]);

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
      <CardContent className="p-0 pb-1">
        <div ref={containerRef} className={cn('w-full h-full min-h-0 overflow-hidden', className)}>
          <BillboardJS bb={bb} ref={chartComponent} className="h-full w-full" options={options} />
        </div>
      </CardContent>
    </Card>
  );
};
