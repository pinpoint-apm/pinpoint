import cytoscape from 'cytoscape';

/**
 * 그려진 링크를 새 데이터의 링크와 맞춘다. 없어진 링크는 지우고, 양 끝이 그려져 있는 새 링크는 더한다.
 *
 * 노드를 맞추는 것과 따로 한다. 노드가 그대로여도 링크만 생기거나 없어질 수 있다 — 실시간 보기에서
 * 펼친 group 안쪽에 새 호출이 생기면 노드는 같고 자식끼리의 링크만 늘어난다. 노드를 더하고 지울 때만
 * 링크를 함께 다루면 그런 링크는 끝내 그려지지 않거나 지워지지 않는다.
 *
 * 남는 링크의 데이터(라벨의 수치)는 건드리지 않는다. 그리는 쪽이 `cy.data(id)`에서 읽으므로 그것을
 * 바꾸면 된다(`ServerMap`).
 */
export const syncEdges = (
  cy: cytoscape.Core,
  edges: { data: { id: string; source: string; target: string } }[],
) => {
  const nextById = new Map(edges.map(({ data }) => [data.id, data]));

  cy.edges()
    .filter((edge) => {
      const next = nextById.get(edge.id());
      // id가 같아도 양 끝이 바뀌었으면 cytoscape에서는 끝을 바꿀 수 없으므로 지우고 다시 더한다.
      return !next || next.source !== edge.source().id() || next.target !== edge.target().id();
    })
    .remove();

  nextById.forEach((data) => {
    const isDrawn = cy.getElementById(data.id).nonempty();
    const hasEnds =
      cy.getElementById(data.source).nonempty() && cy.getElementById(data.target).nonempty();
    if (!isDrawn && hasEnds) {
      cy.add({ data });
    }
  });
};
