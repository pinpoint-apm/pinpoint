import { isValid } from 'date-fns';
import { escapeHTMLEntities, formatNewLinedDateString } from '@pinpoint-fe/ui/src/utils';

// category x축(timestamp) 라벨을 2줄짜리 날짜/시간 문자열로 포맷한다.
export const formatCategoryDateLabel = (value: number | string) => {
  const ts = typeof value === 'string' ? Number(value) : value;
  const date = new Date(ts);
  if (isValid(date)) {
    return formatNewLinedDateString(date);
  }
  return String(value);
};

// trigger: 'axis' 툴팁의 공통 포맷터. 날짜 헤더 + 시리즈별 색상 스와치/값 행을 그린다.
// 값 포맷은 차트마다 다르므로 formatValue 로 주입받는다.
export const formatAxisTooltip = (
  // echarts 툴팁 params 는 라이브러리 타입이 느슨해 any 로 받고 내부에서 좁힌다.
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  params: any,
  formatValue: (value: number) => string,
) => {
  if (!Array.isArray(params) || params.length === 0) return '';
  const axisValue = params[0].axisValue;
  const axisValueNum = typeof axisValue === 'number' ? axisValue : Number(axisValue);
  const dateStr = isValid(new Date(axisValueNum))
    ? formatNewLinedDateString(axisValueNum).replace('\n', ' ')
    : String(axisValue);
  const rows = params
    .map(
      (param: {
        value?: number | [number, number] | null;
        seriesName?: string;
        color?: string;
      }) => {
        const yValue = typeof param.value === 'number' ? param.value : param.value?.[1];
        if (yValue == null) return null;
        return `<div style="display: flex; justify-content: space-between; gap: 12px; align-items: center;">
                  <div style="display: flex; gap: 5px; align-items: center;">
                    <div style="width: 10px; height: 10px; background: ${param.color};"></div>${escapeHTMLEntities(String(param.seriesName ?? ''))}
                  </div>
                  <div>${formatValue(yValue)}</div>
                </div>`;
      },
    )
    .filter(Boolean)
    .join('');
  return `<div>
            <div style="margin-bottom: 5px;"><strong>${escapeHTMLEntities(dateStr)}</strong></div>
            ${rows}
          </div>`;
};
