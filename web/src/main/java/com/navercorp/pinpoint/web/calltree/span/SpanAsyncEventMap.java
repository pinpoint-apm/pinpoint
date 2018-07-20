package com.navercorp.pinpoint.web.calltree.span;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navercorp.pinpoint.common.server.bo.SpanEventBo;

public class SpanAsyncEventMap {

    private final Map<Integer, Map<Short, List<SpanEventBo>>> map = new HashMap<>();
    private int size = 0;

    public boolean add(final SpanEventBo spanEvent) {
        if (!spanEvent.isAsync()) {
            return false;
        }

        final int id = spanEvent.getAsyncId();
        Map<Short, List<SpanEventBo>> subMap = map.computeIfAbsent(id, k -> new HashMap<>());

        final short sequence = spanEvent.getAsyncSequence();
        List<SpanEventBo> list = subMap.get(sequence);
        if (list == null) {
            list = new ArrayList<>();
            list.add(spanEvent);
            subMap.put(sequence, list);
        } else {
            list.add(spanEvent);
        }

        this.size++;
        return true;
    }

    public void sort() {
        for (Map<Short, List<SpanEventBo>> subMap : map.values()) {
            for (List<SpanEventBo> list : subMap.values()) {
                list.sort(new Comparator<SpanEventBo>() {
                    public int compare(SpanEventBo source, SpanEventBo target) {
                        return source.getSequence() - target.getSequence();
                    }
                });
            }
        }
    }

    public Collection<List<SpanEventBo>> get(final int asyncId) {
        final Map<Short, List<SpanEventBo>> subMap = map.get(asyncId);
        if (subMap == null) {
            return Collections.emptyList();
        }
        return subMap.values();
    }

    public int size() {
        return size;
    }
}