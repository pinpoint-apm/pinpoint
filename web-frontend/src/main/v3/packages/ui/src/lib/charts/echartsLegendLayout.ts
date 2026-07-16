// 하단(bottom) legend를 쓰는 echarts 차트에서, legend가 차트 폭에 따라 몇 줄로 줄바꿈되는지
// 계산해 grid.bottom을 동적으로 잡기 위한 상수/헬퍼. 이렇게 해야 legend를 하단에 둔 채로
// 2줄짜리 x축 라벨과 겹치지 않는다.
const LEGEND_FONT = '12px sans-serif'; // echarts legend 기본 폰트
export const LEGEND_ICON_WIDTH = 10; // legend.itemWidth
const LEGEND_ICON_TEXT_GAP = 5; // 아이콘과 텍스트 사이 기본 간격
// legend.itemGap. echarts 는 이 값을 가로 항목 간격뿐 아니라 줄바꿈된 줄 사이 세로 간격으로도
// 쓴다(layout.js: 줄바꿈 시 y += 줄높이 + itemGap). 값이 크면 여러 줄 legend 의 줄 간격이 벌어진다.
export const LEGEND_ITEM_GAP = 8; // legend.itemGap
const LEGEND_PADDING = 5; // legend 기본 좌우 padding
const LEGEND_ROW_CONTENT_HEIGHT = 16; // legend 한 줄 텍스트/아이콘 높이 (12px 폰트 실측, 줄 사이 간격 제외)
const X_AXIS_LABEL_HEIGHT = 38; // x축 2줄(날짜/시간) 라벨 높이 + 여백
const BOTTOM_GAP = 4; // x축 라벨과 legend 사이 간격

const measureCanvas = typeof document !== 'undefined' ? document.createElement('canvas') : null;
const measureCtx = measureCanvas?.getContext('2d') ?? null;

const getLegendRowCount = (names: string[], availableWidth: number) => {
  if (!measureCtx || availableWidth <= 0 || names.length === 0) return 1;
  measureCtx.font = LEGEND_FONT;

  let rows = 1;
  let lineWidth = 0;
  for (const name of names) {
    const textWidth = measureCtx.measureText(name).width;
    const itemWidth = LEGEND_ICON_WIDTH + LEGEND_ICON_TEXT_GAP + textWidth;
    const nextWidth = lineWidth === 0 ? itemWidth : lineWidth + LEGEND_ITEM_GAP + itemWidth;
    if (nextWidth > availableWidth && lineWidth > 0) {
      rows += 1;
      lineWidth = itemWidth;
    } else {
      lineWidth = nextWidth;
    }
  }
  return rows;
};

// 하단(bottom) legend 의 공통 설정. names(=legend 항목)만 넘기면 icon/itemWidth/itemGap 등 스타일은
// 모든 차트에서 동일한 값을 쓴다. show 같은 차트별 옵션은 extra 로 덮어쓴다.
export const buildBottomLegend = (names: string[], extra?: Record<string, unknown>) => ({
  data: names,
  bottom: 0,
  icon: 'square' as const,
  itemWidth: LEGEND_ICON_WIDTH,
  itemHeight: 10,
  itemGap: LEGEND_ITEM_GAP,
  ...extra,
});

// 하단 legend 자체의 높이(px). 줄바꿈된 줄 수를 반영한다. 마지막 줄엔 trailing gap 이 없으므로
// rows * (줄높이 + gap) 로 잡으면 gap 하나만큼 과다 예약된다.
export const getLegendHeight = (names: string[], containerWidth: number) => {
  if (names.length === 0) return 0;
  const availableWidth = containerWidth - LEGEND_PADDING * 2;
  const rows = getLegendRowCount(names, availableWidth);
  return rows * LEGEND_ROW_CONTENT_HEIGHT + Math.max(0, rows - 1) * LEGEND_ITEM_GAP;
};

// containLabel 을 쓰지 않는 차트(ChartCore 등)용 grid.bottom. x축 2줄 라벨 높이 + legend 높이를
// 함께 예약한다. containLabel 을 쓰는 차트는 x축 라벨이 grid 안에서 처리되므로 getLegendHeight 만 쓰면 된다.
export const getGridBottom = (names: string[], containerWidth: number) => {
  // 시리즈(=legend 항목)가 없으면(빈/No Data 상태) legend 공간을 예약하지 않는다.
  if (names.length === 0) return X_AXIS_LABEL_HEIGHT;
  return X_AXIS_LABEL_HEIGHT + BOTTOM_GAP + getLegendHeight(names, containerWidth);
};
