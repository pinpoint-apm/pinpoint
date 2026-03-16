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

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.OpenTelemetryServiceTypeCategory;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Node {
    private static final Logger logger = LogManager.getLogger(Node.class);

    private final SpanBo span;
    private final SpanCallTree spanCallTree;
    private final boolean opentelemetry;
    private final List<Align> alignList;
    private final SpanAsyncEventMap asyncSpanEventMap;

    private boolean linked = false;
    private boolean corrupted = false;


    public Node(final SpanBo span) {
        this.span = Objects.requireNonNull(span, "span");
        this.opentelemetry = OpenTelemetryServiceTypeCategory.contains(span.getServiceType());
        this.spanCallTree = new SpanCallTree(new SpanAlign(span, false, opentelemetry));
        alignList = buildAlignList(span);
        asyncSpanEventMap = SpanAsyncEventMap.build(span);
    }

    public SpanBo getSpanBo() {
        return span;
    }

    public SpanCallTree getSpanCallTree() {
        return spanCallTree;
    }

    public boolean isLinked() {
        return linked;
    }

    public void setLinked(boolean linked) {
        this.linked = linked;
    }

    public boolean isCorrupted() {
        return corrupted;
    }

    public void setCorrupted(boolean corrupted) {
        this.corrupted = corrupted;
    }

    public boolean isOpentelemetry() {
        return opentelemetry;
    }

    @Override
    public String toString() {
        return "Node{" + "applicationName" + span.getApplicationName() +
                ", agentId=" + span.getAgentId() +
                ", parentSpanId=" + span.getParentSpanId() +
                ", spanId=" + span.getSpanId() +
                ", startTime=" + span.getStartTime() +
                ", elapsed=" + span.getElapsed() +
                ", collectorAcceptTime=" + span.getCollectorAcceptTime() +
                ", linked=" + linked +
                '}';
    }

    private Align newSpanEventAlign(SpanEventBo spanEventBo) {
        Objects.requireNonNull(spanEventBo, "spanEventBo");
        return new SpanEventAlign(span, spanEventBo, opentelemetry);
    }

    private Align newSpanEventAlign(SpanChunkBo spanChunkBo, SpanEventBo spanEventBo) {
        Objects.requireNonNull(spanChunkBo, "spanChunkBo");
        Objects.requireNonNull(spanEventBo, "spanEventBo");
        return new SpanChunkEventAlign(span, spanChunkBo, spanEventBo, opentelemetry);
    }

    private List<Align> buildAlignList(SpanBo spanBo) {
        List<SpanEventBo> spanEventBoList = spanBo.getSpanEventBoList();
        List<Align> alignList = buildAlignList(spanEventBoList);
        List<SpanChunkBo> spanChunkBoList = spanBo.getSpanChunkBoList();
        List<Align> chunkSpanEventList = buildSpanChunkBaseAligns(spanChunkBoList);

        return mergeAndSort(spanBo, alignList, chunkSpanEventList);
    }

    private List<Align> buildAlignList(List<SpanEventBo> spanEventBoList) {
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return Collections.emptyList();
        }

        List<Align> alignList = new ArrayList<>(spanEventBoList.size());
        for (SpanEventBo spanEventBo : spanEventBoList) {
            if (logger.isDebugEnabled()) {
                logger.debug("Populate spanEvent{seq={}, depth={}, event={}}", spanEventBo.getSequence(), spanEventBo.getDepth(), spanEventBo);
            }
            final Align spanEventAlign = this.newSpanEventAlign(spanEventBo);
            alignList.add(spanEventAlign);
        }
        return alignList;
    }

    private List<Align> mergeAndSort(SpanBo spanBo, List<Align> alignList1, List<Align> alignList2) {
        List<Align> mergedList = ListUtils.union(alignList1, alignList2);
        if (mergedList.size() > 1 && OpenTelemetryServiceTypeCategory.contains(spanBo.getServiceType())) {
            return findOpenTelemetryChildSpanEvent(spanBo, mergedList, spanBo.getSpanId(), 1, (short) 0);
        }

        mergedList.sort(AlignComparator.INSTANCE);
        return mergedList;
    }

    private List<Align> findOpenTelemetryChildSpanEvent(SpanBo spanBo, List<Align> alignList, long parentSpanId, int depth, short sequence) {
        if (alignList.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Align> sortedAlignList = new ArrayList<>();
        final List<Align> childAlignList = new ArrayList<>();
        for (Align align : alignList) {
            if (parentSpanId == align.getOpenTelemetryParentSpanId()) {
                childAlignList.add(align);
            }
        }
        alignList.removeAll(childAlignList);
        childAlignList.sort(AlignComparator.OPENTELEMETRY_START_TIME);
        for (Align align : childAlignList) {
            // set depth, sequence, startElapsed
            final SpanEventBo spanEventBo = align.getSpanEventBo();
            spanEventBo.setDepth(depth);
            spanEventBo.setSequence(sequence++);
            final long eventStartTime = TimeUnit.NANOSECONDS.toMillis(align.getOpenTelemetryStartTime());
            final int startElapsed = (int) (eventStartTime - spanBo.getStartTime());
            spanEventBo.setStartElapsed(startElapsed);
            sortedAlignList.add(align);
            final List<Align> list = findOpenTelemetryChildSpanEvent(spanBo, alignList, align.getOpenTelemetrySpanId(), depth + 1, sequence);
            sequence += (short) list.size();
            sortedAlignList.addAll(list);
        }

        return sortedAlignList;
    }

    public List<Align> getAlignList() {
        return alignList;
    }

    public SpanAsyncEventMap getAsyncSpanEventMap() {
        return asyncSpanEventMap;
    }

    private List<Align> buildSpanChunkBaseAligns(List<SpanChunkBo> spanChunkBoList) {
        if (CollectionUtils.isEmpty(spanChunkBoList)) {
            return Collections.emptyList();
        }
        List<Align> alignList = new ArrayList<>();
        for (SpanChunkBo chunkBo : spanChunkBoList) {
            if (chunkBo.isAsyncSpanChunk()) {
                // asyncSpanEvent
                continue;
            }
            List<SpanEventBo> spanEventList = chunkBo.getSpanEventBoList();
            for (SpanEventBo spanEventBo : spanEventList) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Populate spanEvent{seq={}, depth={}, event={}}", spanEventBo.getSequence(), spanEventBo.getDepth(), spanEventBo);
                }
                final Align spanEventAlign = this.newSpanEventAlign(chunkBo, spanEventBo);
                alignList.add(spanEventAlign);
            }
        }
        return alignList;
    }
}
