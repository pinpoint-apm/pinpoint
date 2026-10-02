import cytoscape from 'cytoscape';
import { syncEdges } from '../../core/edge';

const createCy = () => {
  const cy = cytoscape({ headless: true });
  cy.add([{ data: { id: 'a' } }, { data: { id: 'b' } }, { data: { id: 'c' } }]);
  cy.add({ data: { id: 'a~b', source: 'a', target: 'b' } });
  return cy;
};

const edgeIds = (cy: cytoscape.Core) => cy.edges().map((edge) => edge.id());

describe('syncEdges', () => {
  it('adds a link between nodes that were already drawn', () => {
    const cy = createCy();
    const isChanged = syncEdges(cy, [
      { data: { id: 'a~b', source: 'a', target: 'b' } },
      { data: { id: 'b~c', source: 'b', target: 'c' } },
    ]);

    expect(edgeIds(cy).sort()).toEqual(['a~b', 'b~c']);
    expect(isChanged).toBe(true);
  });

  it('removes a link that is gone while its nodes stay', () => {
    const cy = createCy();
    const isChanged = syncEdges(cy, []);

    expect(edgeIds(cy)).toEqual([]);
    expect(isChanged).toBe(true);
    expect(cy.nodes()).toHaveLength(3);
  });

  it('keeps a link that is still there', () => {
    const cy = createCy();
    const before = cy.getElementById('a~b');
    const isChanged = syncEdges(cy, [{ data: { id: 'a~b', source: 'a', target: 'b' } }]);

    expect(cy.getElementById('a~b').same(before)).toBe(true);
    expect(isChanged).toBe(false);
  });

  it('does not add a link whose end is not drawn', () => {
    const cy = createCy();
    const isChanged = syncEdges(cy, [
      { data: { id: 'a~b', source: 'a', target: 'b' } },
      { data: { id: 'a~x', source: 'a', target: 'x' } },
    ]);

    expect(edgeIds(cy)).toEqual(['a~b']);
    expect(isChanged).toBe(false);
  });

  it('redraws a link whose ends changed under the same id', () => {
    const cy = createCy();
    const isChanged = syncEdges(cy, [{ data: { id: 'a~b', source: 'a', target: 'c' } }]);

    expect(cy.getElementById('a~b').target().id()).toBe('c');
    expect(isChanged).toBe(true);
  });
});
