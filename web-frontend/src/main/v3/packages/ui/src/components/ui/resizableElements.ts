/**
 * react-resizable-panels 4 는 크기 prop 의 **숫자를 픽셀로** 읽는다. 3 까지는 퍼센트였다.
 * 호출부는 계속 퍼센트 숫자를 쓰므로 경계에서 문자열로 바꿔 넘긴다. 이 변환이 빠지면 타입
 * 검사로는 잡히지 않고 패널 제약만 조용히 어긋난다(`minSize={20}` → 20% 가 아니라 20px).
 */
export const toPanelPercent = (size: number | string | undefined) =>
  typeof size === 'number' ? `${size}%` : size;

/**
 * react-resizable-panels 4 에서 `getPanelGroupElement`/`getPanelElement`/
 * `getResizeHandleElement` 가 없어졌다. 요소에 붙는 data 속성과 id 는 그대로라 같은 의미의
 * 조회를 여기서 유지한다.
 *
 * ref 로 대체할 수 없는 이유: 그룹 **밖에서** id 로 패널을 찾아야 하는 곳이 있다
 * (실시간 보기가 서버맵 패널의 현재 폭을 읽는다).
 *
 * 컴포넌트가 아니라서 `resizable.tsx` 와 파일을 나눠 둔다(fast refresh).
 */
const findByIdAndAttribute = (attribute: string, id: string) => {
  if (typeof document === 'undefined') {
    return null;
  }
  // `[id="..."]` 는 `#id` 선택자와 달리 값 안의 점·콜론을 그대로 받는다(이 앱의 id 는
  // `pp.serverMapHorizontalResizable` 처럼 점을 포함한다). 따옴표와 역슬래시만 막아 준다.
  const value = id.replace(/(["\\])/g, '\\$1');

  return document.querySelector<HTMLElement>(`[${attribute}][id="${value}"]`);
};

export const getResizablePanelGroupElement = (id: string) => findByIdAndAttribute('data-group', id);

export const getResizablePanelElement = (id: string) => findByIdAndAttribute('data-panel', id);

export const getResizableHandleElement = (id: string) => findByIdAndAttribute('data-separator', id);
