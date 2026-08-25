import { TraceViewerData } from '@pinpoint-fe/ui/src/constants';
import { FlameNodeType } from '../../FlameGraph/FlameNode';

/**
 * Turns the trace viewer's flat event list into the flame graph's forest: one group per `tid`, so
 * async work drawn on its own thread becomes its own band of rows. Pure, so it can be unit-tested
 * in isolation — same reason as the Call Tree's `timeline.ts`.
 */
export const genFlameGraphData = (data?: TraceViewerData.Response | null) => {
  let result: FlameNodeType<TraceViewerData.TraceEvent>[][] = [];
  if (data) {
    const traceEvents = data?.traceEvents || [];
    const mapByTid: { [key: number]: TraceViewerData.TraceEvent[] } = {};

    traceEvents.forEach((item) => {
      const { tid } = item;

      if (mapByTid[tid]) {
        mapByTid[tid].push(item);
      } else {
        mapByTid[tid] = [];
        mapByTid[tid].push(item);
      }
    });

    result = Object.values(mapByTid).map((traceEventsByTid) => {
      const roots: FlameNodeType<TraceViewerData.TraceEvent>[] = [];
      const map: { [key: string]: FlameNodeType<TraceViewerData.TraceEvent> } = {};

      traceEventsByTid.forEach((item) => {
        const { name } = item;

        if (name !== 'Async Trace') {
          const { id } = item.args;
          map[id] = {
            id,
            children: [],
            start: item.ts / 1000,
            duration: item.dur / 1000,
            detail: item,
            name,
          };
        }
      });

      // Each node lands in exactly one place: under its parent, or in the roots when the parent
      // is not part of this tid group. Walking the raw events instead would attach a node again
      // for every extra event that reuses its record's args — the backend emits "Async Trace"
      // arrow events (the start carries the *parent* record's args, the end the child's) and
      // exception events, so those nodes were drawn twice and collided on their React key.
      Object.values(map).forEach((item) => {
        const { parentId } = item.detail.args;
        const parent = map[parentId];
        if (parent) {
          parent.children.push(item);
        } else {
          roots.push(item);
        }
      });
      return roots;
    });
  }

  return result;
};
