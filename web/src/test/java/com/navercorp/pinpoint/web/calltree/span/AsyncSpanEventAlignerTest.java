package com.navercorp.pinpoint.web.calltree.span;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.navercorp.pinpoint.common.bo.SpanEventBo;

public class AsyncSpanEventAlignerTest {

    private final List<SpanEventBo> asyncCallSpanEventList = new ArrayList<SpanEventBo>();
    private final AsyncSpanEventAligner aligner = new AsyncSpanEventAligner();

    @Before
    public void before() {
        // sync, sync, sycn, async, async, sync
        asyncCallSpanEventList.add(makeSpanEventBo((short) 1, -1, -1));
        asyncCallSpanEventList.add(makeSpanEventBo((short) 2, -1, -1));
        asyncCallSpanEventList.add(makeSpanEventBo((short) 3, -1, 1));
        asyncCallSpanEventList.add(makeSpanEventBo((short) 1,  1, -1));
        asyncCallSpanEventList.add(makeSpanEventBo((short) 2,  1, -1));
        asyncCallSpanEventList.add(makeSpanEventBo((short) 4, -1, -1));
    }

    @Test
    public void init() {
        Map<Integer, List<SpanEventBo>> map = aligner.init(asyncCallSpanEventList);

        List<SpanEventBo> syncList = map.get(-1);
        assertNotNull(syncList);
        assertEquals(4, syncList.size());
        assertEquals(1, syncList.get(0).getSequence());
        assertEquals(2, syncList.get(1).getSequence());
        assertEquals(3, syncList.get(2).getSequence());
        assertEquals(4, syncList.get(3).getSequence());

        List<SpanEventBo> asyncList = map.get(1);
        assertNotNull(asyncList);
        assertEquals(2, asyncList.size());
        assertEquals(1, asyncList.get(0).getSequence());
        assertEquals(2, asyncList.get(1).getSequence());
    }

    @Test
    public void findRoot() {
        Map<Integer, List<SpanEventBo>> map = aligner.init(asyncCallSpanEventList);
        List<SpanEventBo> rootList = aligner.findRoot(map);

        assertNotNull(rootList);
        assertEquals(-1, rootList.get(0).getAsyncId());
    }

    @Test
    public void sort() {
        List<SpanEventBo> list = aligner.sort(asyncCallSpanEventList);
        assertNotNull(list);

        assertEquals(6, list.size());
        assertEquals(-1, list.get(0).getAsyncId());
        assertEquals(-1, list.get(1).getAsyncId());
        assertEquals(-1, list.get(2).getAsyncId());
        assertEquals(1, list.get(3).getAsyncId());
        assertEquals(1, list.get(4).getAsyncId());
        assertEquals(-1, list.get(5).getAsyncId());
    }

    private SpanEventBo makeSpanEventBo(final short sequence, final int asyncId, final int nextAsyncId) {
        SpanEventBo event = new SpanEventBo();
        event.setSequence(sequence);

        event.setAsyncId(asyncId);
        event.setNextAsyncId(nextAsyncId);

        return event;
    }
}
