/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Node {
    private static final Logger logger = LoggerFactory.getLogger(Node.class);

    private final SpanBo span;
    private final SpanCallTree spanCallTree;
    private boolean linked = false;
    private boolean corrupted = false;

    private List<Align> alignList;
    private SpanAsyncEventMap asyncSpanEventMap;

    public Node(final SpanBo span, final SpanCallTree spanCallTree) {
        this.span = span;
        this.spanCallTree = spanCallTree;
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
        final StringBuilder sb = new StringBuilder("{");
        sb.append("parentSpanId=").append(span.getParentSpanId());
        sb.append(", spanId=").append(span.getSpanId());
        sb.append(", startTime=").append(span.getStartTime());
        sb.append(", elapsed=").append(span.getElapsed());
        sb.append(", linked=").append(linked);
        sb.append('}');
        return sb.toString();
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

    public static List<Node> newNodeList(List<SpanBo> spans) {
        final List<Node> nodeList = new ArrayList<>(spans.size());
        // init sorted node list
        for (SpanBo span : spans) {
            SpanCallTree spanCallTree = new SpanCallTree(new SpanAlign(span));
            final Node node = Node.build(span, spanCallTree);
            nodeList.add(node);
        }

        // sort
        nodeList.sort(new Comparator<Node>() {
            @Override
            public int compare(Node first, Node second) {
                return (int) (first.getSpanBo().getStartTime() - second.getSpanBo().getStartTime());
            }
        });
        return nodeList;
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

        return mergeAndSort(alignList, chunkSpanEventList);
    }

    private static List<Align> mergeAndSort(List<Align> alignList1, List<Align> alignList2) {

        List<Align> mergedList = new ArrayList<>(alignList1.size() + alignList2.size());
        mergedList.addAll(alignList1);
        mergedList.addAll(alignList2);

        mergedList.sort(AlignComparator.INSTANCE);
        return mergedList;
    }

    private List<Align> buildAlignList(List<SpanEventBo> spanEventBoList) {
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return Collections.emptyList();
        }

        List<Align> alignList = new ArrayList<>();
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
                if (logger.isDebugEnabled()) {
                    logger.debug("Populate spanEvent{seq={}, depth={}, event={}}", spanEventBo.getSequence(), spanEventBo.getDepth(), spanEventBo);
                }
                final Align spanEventAlign = this.newSpanEventAlign(chunkBo, spanEventBo);
                alignList.add(spanEventAlign);
            }
        }
        return alignList;
    }
}
