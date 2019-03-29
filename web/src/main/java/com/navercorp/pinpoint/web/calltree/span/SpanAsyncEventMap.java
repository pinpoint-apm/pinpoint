package com.navercorp.pinpoint.web.calltree.span;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navercorp.pinpoint.common.server.bo.LocalAsyncIdBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;

public class SpanAsyncEventMap {

    private final Map<Integer, Map<Integer, List<Align>>> map = new HashMap<>();
    private int size = -1;

    public static SpanAsyncEventMap build(SpanBo spanBo) {
        final List<SpanChunkBo> spanChunkBoList = spanBo.getSpanChunkBoList();
        if (CollectionUtils.isEmpty(spanChunkBoList)) {
            return new SpanAsyncEventMap();
        }

        final SpanAsyncEventMap spanAsyncEventMap = new SpanAsyncEventMap();
        for (SpanChunkBo spanChunk : spanChunkBoList) {
            if (!spanChunk.isAsyncSpanChunk()) {
                continue;
            }

            if (!spanAsyncEventMap.add(spanBo, spanChunk)) {
                throw new IllegalStateException("unexpected SpanChunk:" + spanChunk);
            }
        }
        spanAsyncEventMap.sort();
        return spanAsyncEventMap;
    }

    private boolean add(SpanBo spanBo, final SpanChunkBo spanChunkBo) {
        final LocalAsyncIdBo localAsyncId = spanChunkBo.getLocalAsyncId();
        if (localAsyncId  == null) {
            return false;
        }

        List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return false;
        }

        final List<Align> alignList = toAlignList(spanBo, spanChunkBo, spanEventBoList);
        final int id = localAsyncId.getAsyncId();

        Map<Integer, List<Align>> subMap = map.computeIfAbsent(id, k -> new HashMap<>());
        final int sequence = localAsyncId.getSequence();
        final List<Align> exist = subMap.get(sequence);
        if (exist != null) {
            exist.addAll(alignList);
        } else {
            subMap.put(sequence, alignList);
        }
        return true;
    }

    private List<Align> toAlignList(SpanBo spanBo, SpanChunkBo spanChunkBo, List<SpanEventBo> spanEventBoList) {
        List<Align> alignList = new ArrayList<>(spanEventBoList.size());
        for (SpanEventBo spanEventBo : spanEventBoList) {
            SpanChunkEventAlign spanChunkEventAlign = new SpanChunkEventAlign(spanBo, spanChunkBo, spanEventBo);
            alignList.add(spanChunkEventAlign);
        }
        return alignList;
    }

    private void sort() {
        for (Map<Integer, List<Align>> subMap : map.values()) {
            for (List<Align> alignList : subMap.values()) {
                alignList.sort(AlignComparator.INSTANCE);
            }
        }
    }

    private int calculateSize() {
        int size = 0;
        Collection<Map<Integer, List<Align>>> values = map.values();
        for (Map<Integer, List<Align>> asyncSpanChunkBoMap : values) {
            for (List<Align> alignList : asyncSpanChunkBoMap.values()) {
                size += alignList.size();
            }
        }
        return size;
    }

    public Collection<List<Align>> getAsyncAlign(final int asyncId) {
        final Map<Integer, List<Align>> subMap = map.get(asyncId);
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