// echarts 차트들이 공통으로 쓰는 option 조각 헬퍼.

interface EmptyMessageGraphicOptions {
  fontSize?: number;
  top?: number | string;
}

// 데이터가 없을 때 차트 가운데 "No Data" 같은 안내 문구를 그리는 graphic 배열을 만든다.
// hasData 가 true 면 텍스트를 비워(빈 문자열) 아무것도 보이지 않게 한다. graphic 요소 자체는 항상
// 하나 유지하므로, replaceMerge 없이 setOption 을 반복 호출해도 이전 문구가 잔존하지 않는다.
export const buildEmptyMessageGraphic = (
  hasData: boolean,
  message: string,
  { fontSize = 18, top = 'middle' }: EmptyMessageGraphicOptions = {},
) => [
  {
    type: 'text' as const,
    left: 'center' as const,
    top,
    style: {
      text: hasData ? '' : message,
      fontSize,
      fill: '#999',
      textAlign: 'center' as const,
    },
  },
];
