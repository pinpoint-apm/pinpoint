/*
 * Copyright 2026 NAVER Corp.
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
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.io.SpanVersion;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link SpanCallTree#add(CallTree)} attaches an async call tree whose root node carries
 * the remaining top-level events as a sibling chain. The time-sorted insert used to
 * overwrite that chain with the displaced sibling, silently dropping every event after
 * the first one whenever the subtree landed in the middle of already-attached children.
 */
public class SpanCallTreeAsyncSiblingChainTest {

    private static final long SPAN_START_TIME = 1000L;

    private ServiceTypeRegistryService serviceTypeRegistryService;

    @BeforeEach
    public void setUp() {
        serviceTypeRegistryService = Mockito.mock(ServiceTypeRegistryService.class);
        Mockito.when(serviceTypeRegistryService.findServiceType(Mockito.anyInt())).thenReturn(ServiceType.UNKNOWN);
    }

    @Test
    public void insertAsFirstChildKeepsSiblingChain() {
        SpanBo span = newSpan();
        SpanCallTree tree = new SpanCallTree(new SpanAlign(span));
        tree.add(new SpanEventAlign(span, event(0, 1, 300)));
        tree.setCursor(tree.getRoot());

        SpanCallTree asyncTree = asyncTree(span, 100, 150);
        CallTreeNode first = asyncTree.getRoot();
        CallTreeNode second = first.getSibling();
        tree.add(asyncTree);

        CallTreeNode node = tree.getRoot().getChild();
        assertSame(first, node);
        assertSame(second, node.getSibling());
        assertEquals(300, startElapsed(node.getSibling().getSibling()));
        assertNull(node.getSibling().getSibling().getSibling());
        assertSame(tree.getRoot(), second.getParent());
    }

    @Test
    public void insertBetweenSiblingsKeepsSiblingChain() {
        SpanBo span = newSpan();
        SpanCallTree tree = new SpanCallTree(new SpanAlign(span));
        tree.add(new SpanEventAlign(span, event(0, 1, 100)));
        tree.add(new SpanEventAlign(span, event(1, 1, 400)));
        tree.setCursor(tree.getRoot());

        SpanCallTree asyncTree = asyncTree(span, 200, 250);
        CallTreeNode first = asyncTree.getRoot();
        CallTreeNode second = first.getSibling();
        tree.add(asyncTree);

        CallTreeNode node = tree.getRoot().getChild();
        assertEquals(100, startElapsed(node));
        assertSame(first, node.getSibling());
        assertSame(second, node.getSibling().getSibling());
        assertEquals(400, startElapsed(node.getSibling().getSibling().getSibling()));
        assertSame(tree.getRoot(), second.getParent());
    }

    /**
     * Agent flush stream of a WebFlux transaction running three identical WebClient
     * exchanges under {@code Flux.interval}. The async chunks (49,1) (49,3) (49,5) carry
     * the same five events each, but attach in hash order and interleave in time, so one
     * of them hits the middle-insert path and used to lose everything after its first event.
     */
    @Test
    public void interleavedAsyncChunksRenderAllEvents() {
        SpanBo span = new SpanBo();
        span.setParentSpanId(-1);
        span.setSpanId(4996147925326375894L);
        span.setTraceTime(SpanVersion.TRACE_V2, 1785831583185L, 324);
        span.setCollectorAcceptTime(42);

        SpanChunkBo chunk;

        // root span own events
        span.addSpanEvent(event(1, 2, 1, 0, -1L, 5053, 294, 47));
        span.addSpanEvent(event(2, 2, 3, 0, -1L, 5053, 302, -1));
        span.addSpanEvent(event(4, 3, 3, 0, -1L, 5053, 305, -1));
        span.addSpanEvent(event(6, 4, 3, 0, -1L, 6510, 7, 49));
        span.addSpanEvent(event(5, 3, 3, 0, -1L, 5071, 26, -1));
        span.addSpanEvent(event(7, 3, 4, 0, -1L, 5053, 307, -1));
        span.addSpanEvent(event(3, 2, 3, 2, -1L, 6510, 8, 48));
        span.addSpanEvent(event(0, 1, 0, 5, -1L, 1141, 275, 46));

        // first tick
        chunk = newChunk(span, 49, 1);
        chunk.addSpanEvent(event(0, 1, 106, 0, -1L, 5053, 328, 50));
        chunk.addSpanEvent(event(2, 2, 106, 0, -1L, 9155, 322, 52));
        chunk.addSpanEvent(event(1, 1, 106, 1, -1L, 5053, 329, 51));
        chunk.addSpanEvent(event(3, 1, 107, 0, -1L, 9155, 331, 53));
        chunk.addSpanEvent(event(4, 1, 107, 1, -1L, 6510, 4, 54));

        chunk = newChunk(span, 53, 1);
        chunk.addSpanEvent(event(1, 2, 109, 0, -1L, 9155, 340, -1));
        chunk.addSpanEvent(event(0, 1, 109, 0, -1L, 100, 1, -1));

        chunk = newChunk(span, 53, 2);
        chunk.addSpanEvent(event(2, 3, 109, 0, 1967456498312073087L, 9154, 324, -1));
        chunk.addSpanEvent(event(3, 3, 110, 0, -1936314892672012756L, 9153, 349, 55));
        chunk.addSpanEvent(event(1, 2, 109, 1, -1L, 9155, 332, -1));
        chunk.addSpanEvent(event(0, 1, 109, 1, -1L, 100, 1, -1));

        chunk = newChunk(span, 55, 1);
        chunk.addSpanEvent(event(0, 1, 111, 0, -1L, 9155, 325, -1));

        chunk = newChunk(span, 53, 3);
        chunk.addSpanEvent(event(2, 3, 120, 0, -1L, 5053, 352, -1));
        chunk.addSpanEvent(event(1, 2, 118, 4, -1L, 9155, 327, -1));
        chunk.addSpanEvent(event(0, 1, 118, 4, -1L, 100, 1, -1));

        chunk = newChunk(span, 53, 4);
        chunk.addSpanEvent(event(0, 1, 123, 1, -1L, 6510, 8, 60));
        chunk.addSpanEvent(event(2, 2, 125, 0, -1L, 9155, 340, -1));
        chunk.addSpanEvent(event(1, 1, 124, 1, -1L, 6510, 8, 61));

        // second tick
        chunk = newChunk(span, 49, 3);
        chunk.addSpanEvent(event(0, 1, 207, 0, -1L, 5053, 328, 62));
        chunk.addSpanEvent(event(2, 2, 207, 0, -1L, 9155, 322, 64));
        chunk.addSpanEvent(event(1, 1, 207, 0, -1L, 5053, 329, 63));
        chunk.addSpanEvent(event(3, 1, 208, 0, -1L, 9155, 331, 65));
        chunk.addSpanEvent(event(4, 1, 208, 1, -1L, 6510, 4, 66));

        chunk = newChunk(span, 65, 1);
        chunk.addSpanEvent(event(1, 2, 209, 0, -1L, 9155, 340, -1));
        chunk.addSpanEvent(event(0, 1, 209, 0, -1L, 100, 1, -1));

        chunk = newChunk(span, 65, 2);
        chunk.addSpanEvent(event(2, 3, 210, 0, 4342884626692033954L, 9154, 324, -1));
        chunk.addSpanEvent(event(3, 3, 210, 0, 7041348692777740916L, 9153, 349, 67));
        chunk.addSpanEvent(event(1, 2, 209, 1, -1L, 9155, 332, -1));
        chunk.addSpanEvent(event(0, 1, 209, 1, -1L, 100, 1, -1));

        chunk = newChunk(span, 67, 1);
        chunk.addSpanEvent(event(0, 1, 211, 1, -1L, 9155, 325, -1));

        chunk = newChunk(span, 65, 3);
        chunk.addSpanEvent(event(2, 3, 222, 0, -1L, 5053, 352, -1));
        chunk.addSpanEvent(event(1, 2, 220, 6, -1L, 9155, 327, -1));
        chunk.addSpanEvent(event(0, 1, 220, 6, -1L, 100, 1, -1));

        chunk = newChunk(span, 65, 4);
        chunk.addSpanEvent(event(1, 2, 228, 0, -1L, 9155, 340, -1));
        chunk.addSpanEvent(event(0, 1, 228, 0, -1L, 6510, 8, 72));

        // third tick
        chunk = newChunk(span, 49, 5);
        chunk.addSpanEvent(event(0, 1, 305, 0, -1L, 5053, 328, 73));
        chunk.addSpanEvent(event(2, 2, 305, 0, -1L, 9155, 322, 75));
        chunk.addSpanEvent(event(1, 1, 305, 0, -1L, 5053, 329, 74));
        chunk.addSpanEvent(event(3, 1, 306, 0, -1L, 9155, 331, 76));
        chunk.addSpanEvent(event(4, 1, 306, 1, -1L, 6510, 4, 77));

        chunk = newChunk(span, 76, 1);
        chunk.addSpanEvent(event(1, 2, 307, 0, -1L, 9155, 340, -1));
        chunk.addSpanEvent(event(0, 1, 307, 0, -1L, 100, 1, -1));

        chunk = newChunk(span, 76, 2);
        chunk.addSpanEvent(event(2, 3, 308, 0, 2660039654385373933L, 9154, 324, -1));
        chunk.addSpanEvent(event(3, 3, 308, 0, -7283370457917708855L, 9153, 349, 78));
        chunk.addSpanEvent(event(1, 2, 308, 0, -1L, 9155, 332, -1));
        chunk.addSpanEvent(event(0, 1, 308, 0, -1L, 100, 1, -1));

        chunk = newChunk(span, 78, 1);
        chunk.addSpanEvent(event(0, 1, 309, 1, -1L, 9155, 325, -1));

        chunk = newChunk(span, 76, 3);
        chunk.addSpanEvent(event(2, 3, 319, 0, -1L, 5053, 352, -1));
        chunk.addSpanEvent(event(1, 2, 317, 4, -1L, 9155, 327, -1));
        chunk.addSpanEvent(event(0, 1, 317, 4, -1L, 100, 1, -1));

        chunk = newChunk(span, 76, 4);
        chunk.addSpanEvent(event(1, 2, 324, 0, -1L, 9155, 340, -1));
        chunk.addSpanEvent(event(0, 1, 324, 0, -1L, 6510, 8, 83));

        Predicate<SpanBo> filter = SpanFilters.collectorAcceptTimeFilter(42);
        SpanAligner aligner = new SpanAligner(List.of(span), filter, serviceTypeRegistryService);
        CallTree callTree = aligner.align();

        Map<Integer, Integer> eventCountByApiId = new HashMap<>();
        CallTreeIterator iterator = callTree.iterator();
        CallTreeNode node;
        while ((node = iterator.next()) != null) {
            Align align = node.getAlign();
            if (align.isSpan()) {
                continue;
            }
            eventCountByApiId.merge(align.getSpanEventBo().getApiId(), 1, Integer::sum);
        }

        // every event of the three identical (49,n) chunks must survive
        assertEquals(3, eventCountByApiId.get(328), "first event - one per tick");
        assertEquals(3, eventCountByApiId.get(329), "second event - one per tick");
        assertEquals(3, eventCountByApiId.get(322), "third event - one per tick");
        assertEquals(3, eventCountByApiId.get(331), "fourth event - one per tick");
        assertEquals(3, eventCountByApiId.get(4), "fifth event - one per tick");
    }

    private SpanBo newSpan() {
        SpanBo span = new SpanBo();
        span.setTraceTime(SpanVersion.TRACE_V2, SPAN_START_TIME, 500);
        return span;
    }

    // an async call tree whose root node carries the second event as its sibling
    private SpanCallTree asyncTree(SpanBo span, int firstStartElapsed, int secondStartElapsed) {
        SpanCallTree asyncTree = new SpanAsyncCallTree(new SpanAlign(span));
        asyncTree.add(new SpanEventAlign(span, event(0, 1, firstStartElapsed)));
        asyncTree.add(new SpanEventAlign(span, event(1, 1, secondStartElapsed)));
        return asyncTree;
    }

    private int startElapsed(CallTreeNode node) {
        return node.getAlign().getSpanEventBo().getStartElapsed();
    }

    private SpanChunkBo newChunk(SpanBo span, int asyncId, int sequence) {
        SpanChunkBo chunk = new SpanChunkBo();
        chunk.setSpanId(span.getSpanId());
        chunk.setLocalAsyncId(new LocalAsyncIdBo(asyncId, sequence));
        span.addSpanChunkBo(chunk);
        return chunk;
    }

    private SpanEventBo event(int sequence, int depth, int startElapsed) {
        return event(sequence, depth, startElapsed, 0, -1L, 0, 0, -1);
    }

    private SpanEventBo event(int sequence, int depth, int startElapsed, int endElapsed, long nextSpanId, int serviceType, int apiId, int nextAsyncId) {
        SpanEventBo event = new SpanEventBo();
        event.setSequence((short) sequence);
        event.setDepth(depth);
        event.setStartElapsed(startElapsed);
        event.setEndElapsed(endElapsed);
        event.setNextSpanId(nextSpanId);
        event.setServiceType((short) serviceType);
        event.setApiId(apiId);
        event.setNextAsyncId(nextAsyncId);
        return event;
    }
}
