import 'billboard.js/dist/billboard.css';
import React from 'react';
import { SystemMetricMetricInfo } from '@pinpoint-fe/ui/src/constants';
import { useGetSystemMetricChartData, useGetSystemMetricTagsData } from '@pinpoint-fe/ui/src/hooks';
import bb, { ChartOptions, line } from 'billboard.js';
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
  const options: ChartOptions = {
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
        <BillboardJS
          bb={bb}
          ref={chartComponent}
          className={cn('w-full h-full', className)}
          options={options}
        />
      </CardContent>
    </Card>
  );
};
