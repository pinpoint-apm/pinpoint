import cytoscape from 'cytoscape';
import { keyBy } from 'lodash';
import { findDrawnElement, getHighlightTarget } from '../../core/selection';

type Element = { data: { id: string; source?: string; target?: string; memberIds?: string[] } };

/** `ServerMap`처럼 그린 요소와 별도로 `cy.data`에 원본을 담는다. */
const setData = (cy: cytoscape.Core, elements: Element[]) => {
  cy.removeData();
  cy.data(keyBy(elements, 'data.id'));
};

// base → B(묶음 노드). 묶음 링크 base~B가 자식 링크 base~b1을 대신 그린다.
const createCy = () => {
  const cy = cytoscape({ headless: true });
  const elements: Element[] = [
    { data: { id: 'base' } },
    { data: { id: 'B', memberIds: ['b1', 'b2'] } },
    { data: { id: 'base~B', source: 'base', target: 'B', memberIds: ['base~b1', 'base~b2'] } },
  ];
  cy.add(
    elements.map(({ data }) => ({
      data: { id: data.id, source: data.source, target: data.target },
    })),
  );
  setData(cy, elements);
  return { cy, elements };
};

describe('findDrawnElement', () => {
  it('returns the element itself when it is drawn', () => {
    const { cy } = createCy();
    expect(findDrawnElement(cy, 'B').id()).toBe('B');
  });

  it('returns the element that stands in for a member', () => {
    const { cy } = createCy();
    expect(findDrawnElement(cy, 'b1').id()).toBe('B');
    expect(findDrawnElement(cy, 'base~b1').id()).toBe('base~B');
  });

  it('returns an empty collection when nothing stands in for it', () => {
    const { cy } = createCy();
    expect(findDrawnElement(cy, 'x').nonempty()).toBe(false);
    expect(findDrawnElement(cy, '').nonempty()).toBe(false);
  });
});

describe('getHighlightTarget', () => {
  it('prefers the selection over the last clicked element', () => {
    const { cy } = createCy();
    expect(
      getHighlightTarget(cy, { selectedId: 'base~b1', clickedId: 'B', baseNodeId: 'base' }).id(),
    ).toBe('base~B');
  });

  it('falls back to the last clicked element, then to the base node', () => {
    const { cy } = createCy();
    expect(
      getHighlightTarget(cy, { selectedId: 'x', clickedId: 'B', baseNodeId: 'base' }).id(),
    ).toBe('B');
    expect(
      getHighlightTarget(cy, { selectedId: 'x', clickedId: '', baseNodeId: 'base' }).id(),
    ).toBe('base');
  });

  it('moves off a group link once the selected link is no longer one of its members', () => {
    const { cy, elements } = createCy();
    const params = { selectedId: 'base~b1', clickedId: '', baseNodeId: 'base' };
    expect(getHighlightTarget(cy, params).id()).toBe('base~B');

    // 실시간 갱신으로 선택된 자식 링크가 묶음 링크에서 빠진다. 그려진 요소는 그대로다.
    setData(cy, [
      elements[0],
      elements[1],
      { data: { ...elements[2].data, memberIds: ['base~b2'] } },
    ]);

    expect(getHighlightTarget(cy, params).id()).toBe('base');
  });
});
