/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.trace.span;

import com.navercorp.pinpoint.common.server.bo.LocalAsyncIdBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SpanAsyncEventMap {

    private final MutableIntObjectMap<MutableIntObjectMap<List<Align>>> map = IntObjectMaps.mutable.of();
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

        MutableIntObjectMap<List<Align>> subMap = map.getIfAbsentPutWithKey(id, k -> IntObjectMaps.mutable.of());
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
        for (MutableIntObjectMap<List<Align>> subMap : map.values()) {
            for (List<Align> alignList : subMap.values()) {
                alignList.sort(AlignComparator.INSTANCE);
            }
        }
    }

    private int calculateSize() {
        int size = 0;
        for (MutableIntObjectMap<List<Align>> asyncSpanChunkBoMap : map.values()) {
            for (List<Align> alignList : asyncSpanChunkBoMap.values()) {
                size += alignList.size();
            }
        }
        return size;
    }

    public Collection<List<Align>> getAsyncAlign(final int asyncId) {
        final MutableIntObjectMap<List<Align>> subMap = map.get(asyncId);
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