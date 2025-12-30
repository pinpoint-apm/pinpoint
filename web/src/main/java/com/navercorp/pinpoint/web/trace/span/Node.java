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

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
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
    private boolean linked = false;
    private boolean corrupted = false;

    private List<Align> alignList;
    private SpanAsyncEventMap asyncSpanEventMap;

    public Node(final SpanBo span, final SpanCallTree spanCallTree) {
        this.span = Objects.requireNonNull(span, "span");
        this.spanCallTree = Objects.requireNonNull(spanCallTree, "spanCallTree");
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
        return new SpanEventAlign(span, spanEventBo);
    }

    private Align newSpanEventAlign(SpanChunkBo spanChunkBo, SpanEventBo spanEventBo) {
        Objects.requireNonNull(spanChunkBo, "spanChunkBo");
        Objects.requireNonNull(spanEventBo, "spanEventBo");
        return new SpanChunkEventAlign(span, spanChunkBo, spanEventBo);
    }

    public static Node toNode(SpanBo span) {
        SpanCallTree spanCallTree = new SpanCallTree(new SpanAlign(span));
        return Node.build(span, spanCallTree);
    }

    public static Node build(SpanBo spanBo, SpanCallTree spanCallTree) {
        Objects.requireNonNull(spanBo, "spanBo");
        Objects.requireNonNull(spanCallTree, "spanCallTree");

        Node node = new Node(spanBo, spanCallTree);

        node.alignList = buildAlignList(spanBo, node);

        node.asyncSpanEventMap = SpanAsyncEventMap.build(spanBo);

        return node;
    }

    private static List<Align> buildAlignList(SpanBo spanBo, Node node) {

        List<SpanEventBo> spanEventBoList = spanBo.getSpanEventBoList();
        List<Align> alignList = node.buildAlignList(spanEventBoList);

        List<SpanChunkBo> spanChunkBoList = spanBo.getSpanChunkBoList();
        List<Align> chunkSpanEventList = node.buildSpanChunkBaseAligns(spanChunkBoList);

        return mergeAndSort(spanBo, alignList, chunkSpanEventList);
    }

    private static List<Align> mergeAndSort(SpanBo spanBo, List<Align> alignList1, List<Align> alignList2) {
        List<Align> mergedList = ListUtils.union(alignList1, alignList2);
        if (mergedList.size() > 1 && OpenTelemetryServiceTypeCategory.isServer(spanBo.getServiceType())) {
            mergedList.sort(AlignComparator.OPENTELEMETRY);
            short sequence = 0;
            for (Align align : mergedList) {
                SpanEventBo spanEventBo = align.getSpanEventBo();
                for (AnnotationBo annotationBo : spanEventBo.getAnnotationBoList()) {
                    if (AnnotationKey.OPENTELEMETRY_START_TIME.getCode() == annotationBo.getKey()) {
                        if (annotationBo.getValue() instanceof Long) {
                            final long eventStartTime = TimeUnit.NANOSECONDS.toMillis((Long) annotationBo.getValue());
                            // ignored overflow
                            final int startElapsed = (int) (eventStartTime - spanBo.getStartTime());
                            spanEventBo.setStartElapsed(startElapsed);
                        }
                    }
                }
                spanEventBo.setSequence(sequence++);
            }
        }

        mergedList.sort(AlignComparator.INSTANCE);
        return mergedList;
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
