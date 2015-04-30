package com.navercorp.pinpoint.web.calltree.span;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.common.bo.SpanEventBo;

/**
 * Asynchronous event align e.g.) sync sync async (+) sync
 * 
 * @author jaehong-kim
 *
 */
public class AsyncSpanEventAligner {
    private static final int SYNC = -1;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<SpanEventBo> sort(final List<SpanEventBo> spanEventBoList) {
        if (spanEventBoList == null) {
            return null;
        }

        final Map<Integer, List<SpanEventBo>> allEventMap = init(spanEventBoList);
        final List<SpanEventBo> alignedList = new ArrayList<SpanEventBo>();
        populate(allEventMap, findRoot(allEventMap), alignedList);

        return alignedList;
    }

    // initialize map
    Map<Integer, List<SpanEventBo>> init(final List<SpanEventBo> spanEventBoList) {
        final Map<Integer, List<SpanEventBo>> allEventMap = new HashMap<Integer, List<SpanEventBo>>();
        for (SpanEventBo spanEvent : spanEventBoList) {
            final int id = spanEvent.getAsyncId();
            List<SpanEventBo> list = allEventMap.get(id);
            if (list == null) {
                list = new ArrayList<SpanEventBo>();
                list.add(spanEvent);
                allEventMap.put(id, list);
            } else {
                list.add(spanEvent);
            }
        }

        for (List<SpanEventBo> list : allEventMap.values()) {
            Collections.sort(list, new Comparator<SpanEventBo>() {
                public int compare(SpanEventBo source, SpanEventBo target) {
                    return source.getSequence() - target.getSequence();
                }
            });
        }

        return allEventMap;
    }

    // find root event
    List<SpanEventBo> findRoot(final Map<Integer, List<SpanEventBo>> allEventMap) {
        List<SpanEventBo> root = allEventMap.get(SYNC);
        if (root != null) {
            // find root
            return root;
        }

        if (allEventMap.keySet().size() == 1) {
            // only one element
            final int key = new ArrayList<Integer>(allEventMap.keySet()).get(0);
            return allEventMap.remove(key);
        }

        // find has nextAsyncId
        for (Entry<Integer, List<SpanEventBo>> entry : allEventMap.entrySet()) {
            for (SpanEventBo event : entry.getValue()) {
                if (event.getNextAsyncId() != -1) {
                    return allEventMap.remove(entry.getKey());
                }
            }
        }

        // first element
        final int key = new ArrayList<Integer>(allEventMap.keySet()).get(0);
        return allEventMap.remove(key);
    }

    // add async event
    void populate(final Map<Integer, List<SpanEventBo>> allEventMap, final List<SpanEventBo> currentEventList, final List<SpanEventBo> alignedEventList) {
        int sequence = 0;
        for (SpanEventBo spanEvent : currentEventList) {
            if (spanEvent.isAsync()) {
                // check missing event.
                if (sequence != spanEvent.getSequence()) {
                    logger.debug("Parent async event missing & ignored");
                    break;
                }
                sequence++;
            }
            alignedEventList.add(spanEvent);
            final int id = spanEvent.getNextAsyncId();
            if (id != -1) {
                final List<SpanEventBo> subList = allEventMap.remove(id);
                if (subList != null) {
                    populate(allEventMap, subList, alignedEventList);
                }
            }
        }
    }
}