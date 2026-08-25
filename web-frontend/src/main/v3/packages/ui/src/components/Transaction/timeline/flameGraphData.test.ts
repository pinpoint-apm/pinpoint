import { TraceViewerData } from '@pinpoint-fe/ui/src/constants';
import { genFlameGraphData } from './flameGraphData';

const MS = 1000; // the trace viewer reports microseconds

const traceEvent = (e: {
  id: string;
  parentId: string;
  tid?: number;
  name?: string;
  ph?: string;
  start?: number;
  duration?: number;
}): TraceViewerData.TraceEvent =>
  ({
    cat: 'Trace',
    tid: e.tid ?? 1,
    id: '0',
    ts: (e.start ?? 0) * MS,
    ph: e.ph ?? 'X',
    dur: (e.duration ?? 10) * MS,
    s: 't',
    name: e.name ?? `span-${e.id}`,
    cname: '',
    pid: 'app',
    args: { id: e.id, parentId: e.parentId, 'API Type': '', 'Application Name': 'app' },
  }) as TraceViewerData.TraceEvent;

const ids = (nodes: { id: string; children: { id: string }[] }[]) =>
  nodes.map((n) => `${n.id}(${n.children.map((c) => c.id).join(',')})`);

describe('genFlameGraphData', () => {
  test('returns nothing without data', () => {
    expect(genFlameGraphData(null)).toEqual([]);
    expect(genFlameGraphData(undefined)).toEqual([]);
    expect(genFlameGraphData({ traceEvents: [] })).toEqual([]);
  });

  test('nests children under their parent and keeps the parentless node as the root', () => {
    const groups = genFlameGraphData({
      traceEvents: [
        traceEvent({ id: '1', parentId: '-1' }),
        traceEvent({ id: '2', parentId: '1' }),
        traceEvent({ id: '3', parentId: '2' }),
      ],
    });

    expect(groups).toHaveLength(1);
    expect(ids(groups[0])).toEqual(['1(2)']);
    expect(ids(groups[0][0].children)).toEqual(['2(3)']);
  });

  test('splits threads into their own groups', () => {
    const groups = genFlameGraphData({
      traceEvents: [
        traceEvent({ id: '1', parentId: '-1', tid: 1 }),
        traceEvent({ id: '2', parentId: '1', tid: 1 }),
        // async work: its parent lives in the other thread, so it heads its own group
        traceEvent({ id: '3', parentId: '2', tid: 2 }),
      ],
    });

    expect(groups).toHaveLength(2);
    expect(ids(groups[0])).toEqual(['1(2)']);
    expect(ids(groups[1])).toEqual(['3()']);
  });

  test('attaches a node once even when other events reuse its record', () => {
    // The backend emits "Async Trace" arrow events around async work: the start carries the
    // *parent* record's args, the end the child's. Attaching per raw event drew those nodes twice
    // and made two siblings share a React key.
    const groups = genFlameGraphData({
      traceEvents: [
        traceEvent({ id: '1', parentId: '-1', tid: 1 }),
        traceEvent({ id: '2', parentId: '1', tid: 1 }),
        traceEvent({ id: '2', parentId: '1', tid: 1, name: 'Async Trace', ph: 's' }),
        traceEvent({ id: '3', parentId: '2', tid: 2 }),
        traceEvent({ id: '3', parentId: '2', tid: 2, name: 'Async Trace', ph: 'f' }),
      ],
    });

    expect(ids(groups[0])).toEqual(['1(2)']);
    expect(ids(groups[1])).toEqual(['3()']);
  });

  test('never lists the same node twice among siblings', () => {
    const groups = genFlameGraphData({
      traceEvents: [
        traceEvent({ id: '1', parentId: '-1' }),
        traceEvent({ id: '2', parentId: '1' }),
        // an exception row reuses the record of the method it belongs to
        traceEvent({ id: '2', parentId: '1', name: 'exception' }),
      ],
    });

    const childIds = groups[0][0].children.map((c) => c.id);
    expect(childIds).toEqual([...new Set(childIds)]);
    expect(childIds).toEqual(['2']);
  });

  test('converts microseconds to milliseconds', () => {
    const groups = genFlameGraphData({
      traceEvents: [traceEvent({ id: '1', parentId: '-1', start: 1500, duration: 250 })],
    });

    expect(groups[0][0]).toMatchObject({ start: 1500, duration: 250 });
  });
});
