import cytoscape from 'cytoscape';

/**
 * id의 요소가 그려져 있으면 그것을, 없으면 그것을 대신 그리고 있는 요소(`memberIds`에 그 id가
 * 있는 요소)를 찾는다. 둘 다 없으면 빈 컬렉션이다.
 *
 * `memberIds`는 요소의 데이터가 아니라 `cy.data(id)`에 담긴 원본에서 읽는다(`ServerMap`이 매 갱신마다
 * 그곳을 새 데이터로 바꾼다).
 */
export const findDrawnElement = (cy: cytoscape.Core, id: string) => {
  const element = cy.getElementById(id);
  if (!id || element.nonempty()) {
    return element;
  }
  return cy
    .elements()
    .filter((el) => Boolean(cy.data(el.id())?.data?.memberIds?.includes(id)))
    .first() as cytoscape.CollectionReturnValue;
};

/**
 * 데이터가 바뀐 뒤 하이라이트할 요소. 선택(또는 그것을 대신 그리는 요소)이 그래프에 없으면 기준 노드다.
 *
 * 바깥에서 정한 선택(`selectedId`)을 먼저 본다. 마지막으로 클릭한 요소(`clickedId`)는 선택이 아닐 수
 * 있다 — 묶음 노드나 상자를 더블클릭하면 그 첫 클릭이 클릭한 요소가 되는데, 그것들은 조회 대상이
 * 아니고 펼치거나 접으면 곧바로 사라진다.
 */
export const getHighlightTarget = (
  cy: cytoscape.Core,
  {
    selectedId,
    clickedId,
    baseNodeId,
  }: { selectedId?: string; clickedId: string; baseNodeId: string },
) => {
  const fromSelectedId = selectedId ? findDrawnElement(cy, selectedId) : undefined;
  if (fromSelectedId?.nonempty()) {
    return fromSelectedId;
  }
  const fromClickedId = findDrawnElement(cy, clickedId);
  return fromClickedId.nonempty() ? fromClickedId : cy.getElementById(baseNodeId);
};
