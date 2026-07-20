import React from 'react';
import * as echarts from 'echarts/core';
import { BarChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { escapeHTMLEntities } from '@pinpoint-fe/ui/src/utils';
import { useEChartsInstance } from '../../lib/charts/useEChartsInstance';
import { AgentActiveSettingType } from './AgentActiveSetting';
import { AgentActiveData } from './AgentActiveTable';

echarts.use([BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

export const DefaultValue = { yMax: 100 };

export interface AgentActiveChartProps {
  loading?: boolean;
  data?: AgentActiveData[];
  setting?: AgentActiveSettingType;
  clickedActiveThread?: string;
  setClickedActiveThread?: React.Dispatch<React.SetStateAction<string>>;
}

const TICK_COUNT = 4;

// 스택(쌓이는) 순서 = recharts 의 Object.keys(chartConfig).reverse() 와 동일: slow(맨 아래) → 1s(맨 위).
// legend/tooltip 은 이 순서를 뒤집어 1s, 3s, 5s, slow 로 표시한다.
const SERIES = [
  { key: 'slow', label: 'slow', color: '#e67f22' },
  { key: '5s', label: '5s', color: '#ffba00' },
  { key: '3s', label: '3s', color: '#51afdf' },
  { key: '1s', label: '1s', color: '#34b994' },
] as const;
const STACK_ID = 'agentActiveThread';
const BAR_WIDTH = '60%';
// x/y 축 선 색 (은은한 회색 테두리).
const AXIS_LINE_COLOR = '#e5e7eb';
// 선택 컬럼 하이라이트용 내부 시리즈 이름. legend/tooltip 에는 노출하지 않는다.
const HIGHLIGHT_SERIES = '__selected_column__';

// 막대 위에 흰색 가로줄 패턴(decal)을 얹어, 값이 쌓일수록 칸칸(볼륨미터)으로 보이게 한다.
// decal 은 캔버스 패턴이라 요소 수가 고정이라, 실시간 갱신에도 메모리 부담이 없다.
// (recharts 의 흰색 ReferenceLine 게이지 대체)
const SEGMENT_DECAL = {
  color: 'rgba(255, 255, 255, 1)',
  dashArrayX: [1, 0], // 가로로는 끊김 없이 막대 폭 전체
  dashArrayY: [2, 4], // 세로로 흰 줄 2px + 색 칸 4px 반복 → 칸칸이 나뉘어 보임
  rotation: 0,
} as const;

export const AgentActiveChart = ({
  loading,
  data,
  setting,
  clickedActiveThread,
  setClickedActiveThread,
}: AgentActiveChartProps) => {
  const { chartRef, chartInstanceRef, renderRef } = useEChartsInstance();
  const yMax = setting?.yMax || DefaultValue.yMax;

  // 클릭 핸들러는 인스턴스 생성 시 한 번만 등록하고, 최신 값(setter/서버목록)은 ref 로 참조한다.
  const setClickedRef = React.useRef(setClickedActiveThread);
  setClickedRef.current = setClickedActiveThread;
  const serversRef = React.useRef<string[]>([]);

  React.useEffect(() => {
    const chart = chartInstanceRef.current;
    if (!chart) return;

    // 막대뿐 아니라 컬럼 배경 아무 곳이나 클릭해도 그 서버를 선택/해제한다.
    // (recharts onClick 의 activeLabel 처럼 클릭 위치의 x 카테고리를 찾아 토글)
    const zr = chart.getZr();
    const handleZrClick = (event: { offsetX: number; offsetY: number }) => {
      const point = [event.offsetX, event.offsetY];
      if (!chart.containPixel('grid', point)) return; // 그리드(막대 영역) 밖 클릭은 무시
      const index = Math.round(chart.convertFromPixel({ xAxisIndex: 0 }, point[0]) as number);
      const server = serversRef.current[index];
      if (server == null) return;
      setClickedRef.current?.((prev) => (prev === server ? '' : server));
    };
    zr.on('click', handleZrClick);
    return () => {
      zr.off('click', handleZrClick);
    };
  }, [chartInstanceRef]);

  React.useEffect(() => {
    if (!chartInstanceRef.current) return;

    const servers = (data ?? []).map((d) => d.server);
    // 배경 클릭 시 클릭 위치의 x 인덱스로 서버를 찾기 위해 최신 목록을 ref 에 보관한다.
    serversRef.current = servers;

    // trigger:'axis' 툴팁. 헤더는 서버명, 행은 1s→slow 순(스택의 역순)으로, 음수(미수집)는 '-' 로 표시.
    const tooltipFormatter = (
      // echarts 툴팁 params 는 라이브러리 타입이 느슨해 any 로 받는다.
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      params: any,
    ) => {
      if (!Array.isArray(params) || params.length === 0) return '';
      const header = String(params[0].axisValue ?? '');
      // 선택 하이라이트용 내부 시리즈는 tooltip 에서 제외한다.
      const rows = [...params]
        .filter((p: { seriesName?: string }) => p.seriesName !== HIGHLIGHT_SERIES)
        .reverse()
        .map((p: { value?: number; seriesName?: string; color?: string }) => {
          const raw = typeof p.value === 'number' ? p.value : Number(p.value);
          const display = raw < 0 ? '-' : String(raw);
          return `<div style="display: flex; justify-content: space-between; gap: 12px; align-items: center;">
                    <div style="display: flex; gap: 5px; align-items: center;">
                      <span style="display: inline-block; width: 10px; height: 10px; background: ${p.color};"></span>${escapeHTMLEntities(String(p.seriesName ?? ''))}
                    </div>
                    <div>${escapeHTMLEntities(display)}</div>
                  </div>`;
        })
        .join('');
      return `<div><div style="margin-bottom: 5px;"><strong>${escapeHTMLEntities(header)}</strong></div>${rows}</div>`;
    };

    const dataSeries = SERIES.map((s) => ({
      name: s.label,
      type: 'bar' as const,
      stack: STACK_ID,
      barWidth: BAR_WIDTH,
      // 시리즈 레벨엔 색만 둔다. (범례 아이콘은 이 스타일을 쓰므로 decal 을 넣으면 칸칸이 갈라져 보인다)
      itemStyle: { color: s.color },
      // 칸칸(volume) 패턴 decal 과 선택 dimming(opacity) 은 막대 데이터에만 적용한다.
      data: loading
        ? []
        : (data ?? []).map((d) => ({
            value: d[s.key],
            itemStyle: {
              decal: SEGMENT_DECAL,
              opacity: clickedActiveThread ? (d.server === clickedActiveThread ? 1 : 0.3) : 1,
            },
          })),
    }));

    // 선택된 서버 컬럼 뒤에, 막대와 같은 폭(BAR_WIDTH)의 전체 높이 하이라이트 막대를 겹쳐 그린다.
    // barGap:'-100%' 로 스택 막대와 정확히 겹치고, z:0 으로 뒤에 깔린다. (markArea 는 슬롯 전체를
    // 칠해 옆 칸까지 닿으므로 대신 이 방식을 쓴다) 선택이 없으면 빈 데이터로 둔다.
    const highlightSeries = {
      name: HIGHLIGHT_SERIES,
      type: 'bar' as const,
      silent: true,
      z: 0,
      barWidth: BAR_WIDTH,
      barGap: '-100%',
      itemStyle: { color: 'rgba(59, 130, 246, 0.12)' },
      data:
        loading || !clickedActiveThread
          ? []
          : servers.map((server) => (server === clickedActiveThread ? yMax : null)),
    };

    const series = [...dataSeries, highlightSeries];

    const render = () => {
      const chart = chartInstanceRef.current;
      if (!chart) return;

      chart.setOption({
        // 막대가 바닥에서 위로 자라고, 실시간 값 변화도 부드럽게 전환(파도)되도록 애니메이션을 켠다.
        animation: true,
        animationDuration: 500,
        animationDurationUpdate: 300,
        animationEasing: 'cubicOut' as const,
        animationEasingUpdate: 'cubicOut' as const,
        legend: {
          bottom: 0,
          // 기존 UI(rounded-[2px] 정사각형)처럼 모서리가 둥근 '정사각형' 스와치.
          // roundRect 는 이 크기에서 납작해 보여, 1:1 비율의 rounded-square path 를 직접 쓴다.
          icon: 'path://M2,0 L10,0 Q12,0 12,2 L12,10 Q12,12 10,12 L2,12 Q0,12 0,10 L0,2 Q0,0 2,0 Z',
          itemWidth: 12,
          itemHeight: 12,
          itemGap: 12,
          // 클릭해도 시리즈가 토글(회색)되지 않도록 해 한 줄로 온전히 유지한다. (기존 UI 동작)
          selectedMode: false,
          // 스택 역순(1s, 3s, 5s, slow)으로 표시.
          data: [...SERIES].reverse().map((s) => s.label),
        },
        grid: { top: 10, right: 12, bottom: 32, left: 8, containLabel: true },
        xAxis: {
          type: 'category',
          data: servers,
          // 서버명 x축 라벨은 숨긴다(서버 식별은 tooltip/테이블/선택 하이라이트로).
          axisLabel: { show: false },
          axisTick: { show: false },
          // x축(바닥) 선 하나를 표시한다.
          axisLine: { show: true, lineStyle: { color: AXIS_LINE_COLOR } },
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: yMax,
          splitNumber: TICK_COUNT,
          splitLine: { show: false },
          // y축(왼쪽) 선 하나를 표시한다.
          axisLine: { show: true, lineStyle: { color: AXIS_LINE_COLOR } },
          // 각 값(눈금)마다 tick 을 표시한다.
          axisTick: { show: true, lineStyle: { color: AXIS_LINE_COLOR } },
          axisLabel: { margin: 10, formatter: (value: number) => `${Math.round(value)}` },
        },
        tooltip: { trigger: 'axis', confine: true, formatter: tooltipFormatter },
        series,
      });
    };

    renderRef.current = render;
    render();
  }, [data, loading, clickedActiveThread, yMax, chartInstanceRef, chartRef, renderRef]);

  return <div ref={chartRef} className="flex-1 min-w-[50%] min-h-0 overflow-hidden p-1.5" />;
};
