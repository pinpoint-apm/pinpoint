import 'billboard.js/dist/billboard.css';
import React from 'react';
import bb, { ChartOptions, canvas, regions } from 'billboard.js/canvas';
import { isValid } from 'date-fns';
import deepmerge from 'deepmerge';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import BillboardJS, { IChart } from '@billboard.js/react';
import { cn, DEFAULT_CHART_CONFIG } from '../../../lib';
import { InspectorAgentChart, InspectorApplicationChart } from '@pinpoint-fe/ui/src/constants';
import { formatNewLinedDateString } from '@pinpoint-fe/ui/src/utils';

export interface ChartCoreProps {
  data: InspectorAgentChart.Response | InspectorApplicationChart.Response;
  chartOptions?: ChartOptions;
  className?: string;
  emptyMessage?: string;
  style?: React.CSSProperties;
}

export const ChartCore = ({
  data,
  chartOptions = {},
  className,
  emptyMessage = 'No Data',
  style,
}: ChartCoreProps) => {
  const prevData = React.useRef([] as (string | number | null)[][]);
  const chartComponent = React.useRef<IChart>(null);
  const containerRef = React.useRef<HTMLDivElement>(null);
  // canvas 모드는 차트 높이를 스스로 측정해 캔버스 크기로 쓰는데, suspense 리마운트(agent/시간/지표 변경)
  // 순간 높이가 0으로 측정되면 billboard 기본값(320px)으로 그려져 박스보다 커지며 잘린다. 컨테이너 높이를
  // 직접 측정해 size.height로 넘기면 billboard가 측정/폴백 없이 그 높이로 그린다. 컨테이너가 aspect-video든
  // 고정 높이(h-80)든 실제 높이를 따르므로 소비처별 높이를 모두 지원하고, 리사이즈에도 대응한다.
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
  const defaultOptions = {
    // v4 ESM: canvas 렌더링 모드 사용. regions 모듈은 더 이상 자동 번들되지 않아 명시적으로 등록한다
    // (application 차트가 data.regions로 점선 영역을 그림).
    render: {
      mode: canvas(),
    },
    ...regions(),
    ...(measuredHeight ? { size: { height: measuredHeight } } : {}),
    data: {
      x: 'dates',
      columns: [],
      empty: {
        label: {
          text: emptyMessage,
        },
      },
    },
    padding: {
      mode: 'fit',
      top: DEFAULT_CHART_CONFIG.PADDING_TOP,
      bottom: DEFAULT_CHART_CONFIG.PADDING_BOTTOM,
      right: DEFAULT_CHART_CONFIG.PADDING_RIGHT,
      left: DEFAULT_CHART_CONFIG.PADDING_LEFT,
    },
    axis: {
      x: {
        type: 'timeseries',
        tick: {
          count: DEFAULT_CHART_CONFIG.X_AXIS_TICK_COUNT,
          format: (date: Date) => {
            if (isValid(date)) {
              return `${formatNewLinedDateString(date)}`;
            }
            return '';
          },
        },
      },
      y: {
        padding: {
          bottom: 0,
        },
        min: 0,
        default: [0, DEFAULT_CHART_CONFIG.DEFAULT_MAX],
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
    tooltip: {
      linked: true,
      order: '',
    },
    resize: {
      auto: 'parent',
      timer: false,
    },
  };
  const options = deepmerge(defaultOptions, chartOptions);

  React.useEffect(() => {
    const chart = chartComponent.current?.instance;
    const chartData = data
      ? [
          ['dates', ...data.timestamp],
          ...data.metricValues.map(({ fieldName, valueList }) => {
            return [fieldName, ...valueList.map((v) => (v < 0 ? null : v))];
          }),
        ]
      : [];
    const maxData = Math.max(
      ...(chartData
        .slice(1)
        .map((d) => d.slice(1).filter((v) => v !== null))
        .flat() as number[]),
    );

    const prevKeys = prevData.current.slice(1).map(([fieldName]) => fieldName as string);
    const currKeys = chartData.slice(1).map(([fieldName]) => fieldName as string);
    const removedKeys = prevKeys.filter((key) => !currKeys.includes(key));
    const unload = prevKeys.length === 0 ? false : removedKeys.length !== 0;
    chart?.load({
      columns: chartData,
      unload,
    });
    chart?.axis.max(maxData === 0 ? DEFAULT_CHART_CONFIG.DEFAULT_MAX : false);
    chart?.config('tooltip.contents', chartOptions.tooltip?.contents);
    prevData.current = chartData;
  }, [data]);

  return (
    <div
      ref={containerRef}
      style={style}
      className={cn('w-full h-full min-h-0 overflow-hidden', className)}
    >
      <BillboardJS bb={bb} ref={chartComponent} className="h-full w-full" options={options} />
    </div>
  );
};
