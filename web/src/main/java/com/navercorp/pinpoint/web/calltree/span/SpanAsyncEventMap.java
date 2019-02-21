package com.navercorp.pinpoint.web.calltree.span;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navercorp.pinpoint.common.server.bo.LocalAsyncIdBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;

public class SpanAsyncEventMap {

    private final Map<Integer, Map<Integer, SpanChunkBo>> map = new HashMap<>();
    private int size = -1;

    public static SpanAsyncEventMap build(final List<SpanChunkBo> spanChunkBoList) {
        if (CollectionUtils.isEmpty(spanChunkBoList)) {
            return new SpanAsyncEventMap();
        }


        final SpanAsyncEventMap spanAsyncEventMap = new SpanAsyncEventMap();
        for (SpanChunkBo spanChunk : spanChunkBoList) {
            if (!spanAsyncEventMap.add(spanChunk)) {
                throw new IllegalStateException("unexpected SpanChunk:" + spanChunk);
            }
        }
        spanAsyncEventMap.sort();
        return spanAsyncEventMap;
    }

    private boolean add(final SpanChunkBo spanChunkBo) {
        LocalAsyncIdBo localAsyncId = spanChunkBo.getLocalAsyncId();
        if (localAsyncId  == null) {
            return false;
        }

        List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return false;
        }
        final int id = localAsyncId.getAsyncId();
        Map<Integer, SpanChunkBo> subMap = map.computeIfAbsent(id, k -> new HashMap<>());

        final int sequence = localAsyncId.getSequence();
        final SpanChunkBo exist = subMap.get(sequence);
        if (exist != null) {
            exist.addSpanEventBoList(spanChunkBo.getSpanEventBoList());
        } else {
            subMap.put(sequence, spanChunkBo);
        }
        return true;
    }

    private void sort() {
        for (Map<Integer, SpanChunkBo> subMap : map.values()) {
            for (SpanChunkBo spanChunkBo : subMap.values()) {
                List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
                spanEventBoList.sort(new Comparator<SpanEventBo>() {
                    public int compare(SpanEventBo source, SpanEventBo target) {
                        return source.getSequence() - target.getSequence();
                    }
                });
            }
        }
    }

    private int calculateSize() {
        int size = 0;
        Collection<Map<Integer, SpanChunkBo>> values = map.values();
        for (Map<Integer, SpanChunkBo> spanChunkBoMap : values) {
            for (SpanChunkBo spanChunkBo : spanChunkBoMap.values()) {
                size += spanChunkBo.getSpanEventBoList().size();
            }
        }
        return size;
    }

    public Collection<SpanChunkBo> get(final int asyncId) {
        final Map<Integer, SpanChunkBo> subMap = map.get(asyncId);
        if (subMap == null) {
            return Collections.emptyList();
        }
        return subMap.values();
    }

    public int size() {
        if (size != -1) {
            return size;
        }
        size = calculateSize();
        return size;
    }
}