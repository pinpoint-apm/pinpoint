import { isValid } from 'date-fns';
import { InspectorAgentChart, InspectorApplicationChart } from '@pinpoint-fe/ui/src/constants';
import { escapeHTMLEntities, formatNewLinedDateString, getFormat } from '@pinpoint-fe/ui/src/utils';

// 다른 차트와 tooltip/axisPointer 동기화(echarts.connect)를 하지 않는 차트 목록.
// (billboard 의 tooltip.linked: false 대체)
export const TOOLTIP_NOT_LINKED_CHART_LIST = ['Apdex Score'];

// echarts trigger:'axis' tooltip params 는 라이브러리 타입이 느슨하므로 필요한 필드만 좁혀 쓴다.
export interface AxisTooltipParam {
  axisValue?: number | string;
  dataIndex?: number;
  seriesIndex?: number;
  seriesName?: string;
  color?: string;
  value?: number | null;
}

const swatch = (color?: string) =>
  `<span style="display:inline-block;width:10px;height:10px;margin-right:5px;background-color:${color ?? ''};"></span>`;

const row = (color: string | undefined, name: string, value: string) =>
  `<div style="display:flex;justify-content:space-between;gap:12px;align-items:center;">
     <div style="display:flex;align-items:center;">${swatch(color)}${escapeHTMLEntities(name)}</div>
     <div>${escapeHTMLEntities(value)}</div>
   </div>`;

// 렌더링되는 시리즈(params)와 tooltip 전용 데이터(tooltipData)를 합쳐 tooltip HTML 을 만드는 포맷터를 생성한다.
// 시리즈마다 unit 이 다를 수 있어 unitByField 로 각 시리즈의 포맷을 적용한다.
export const createChartTooltipFormatter = ({
  unitByField,
  tooltipData,
}: {
  unitByField: Record<string, string>;
  tooltipData: (InspectorAgentChart.MetricValue | InspectorApplicationChart.MetricValue)[];
}) => {
  return (params: AxisTooltipParam[]) => {
    if (!Array.isArray(params) || params.length === 0) return '';

    const axisValue = params[0].axisValue;
    const axisValueNum = typeof axisValue === 'number' ? axisValue : Number(axisValue);
    const dateStr = isValid(new Date(axisValueNum))
      ? formatNewLinedDateString(axisValueNum).replace('\n', ' ')
      : String(axisValue ?? '');
    const focusIndex = params[0].dataIndex ?? 0;

    const seriesRows = params
      .map((param) => {
        if (param.value == null) return null;
        const name = param.seriesName ?? '';
        return row(param.color, name, getFormat(unitByField[name] ?? '')(param.value));
      })
      .filter(Boolean)
      .join('');

    const extraRows = tooltipData
      .map(({ fieldName, unit, valueList }) =>
        row('#f87171', fieldName, getFormat(unit)(valueList[focusIndex])),
      )
      .join('');

    return `<div>
              <div style="margin-bottom:5px;"><strong>${escapeHTMLEntities(dateStr)}</strong></div>
              ${seriesRows}${extraRows}
            </div>`;
  };
};
