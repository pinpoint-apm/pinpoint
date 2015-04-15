package com.navercorp.pinpoint.web.service;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.common.bo.SpanEventBo;
import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import com.navercorp.pinpoint.web.calltree.span.SpanAligner2;
import com.navercorp.pinpoint.web.util.Stack;
import com.navercorp.pinpoint.web.vo.callstacks.Record;

public class SpanDepthStackTest {
    private static final boolean SYNC = false;
    private static final boolean ASYNC = true;

    private int idGen = 1;

    @Test
    public void test() {
        populate(normal());
        // populate(missingCase1());
        populate(missingSpanEvent());
    }

    private List<SpanAlign> normal() {
        SpanBo span = new SpanBo();
        span.setParentSpanId(-1);
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 0, 1));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 1, 2));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 2, 3));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 3, 4));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 4, 5));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 5, -1));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 6, -1));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 7, -1));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 8, -1));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 9, 4));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 10, -1));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 11, 5));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 12, 6));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 13, 7));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 14, -1));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 15, 4));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 16, -1));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 17, -1));
        
        SpanAligner2 aligner = new SpanAligner2(Arrays.asList(span), 1);
        return aligner.sort();
    }
    
    private List<SpanAlign> missingSpanEvent() {
        SpanBo span = new SpanBo();
        span.setParentSpanId(-1);
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 0, -1));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 1, -1));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 2, -1));
        span.addSpanEvent(makeSpanEventBo(SYNC, (short) 3, -1));
        
        SpanAligner2 aligner = new SpanAligner2(Arrays.asList(span), 1);
        return aligner.sort();
    }
    
    private void populate(List<SpanAlign> spanAlignList) {
        Stack<SpanDepth> stack = new Stack<SpanDepth>();

        // annotation id has nothing to do with spanAlign's seq and thus may be incremented as long as they don't overlap.
        for (int i = 0; i < spanAlignList.size(); i++) {
            final SpanAlign spanAlign = spanAlignList.get(i);
            if (i == 0) {
                if (!spanAlign.isSpan()) {
                    throw new IllegalArgumentException("root is not span");
                }
                final SpanDepth spanDepth = new SpanDepth(spanAlign, getNextId(), spanAlign.getSpanBo().getStartTime());
                stack.push(spanDepth);
            } else {
                final SpanDepth lastSpanDepth = stack.getLast();
                final int parentDepth = lastSpanDepth.getSpanAlign().getDepth();
                final int currentDepth = spanAlign.getDepth();

                if (parentDepth < spanAlign.getDepth()) {
                    // push if parentDepth is smaller
                    final SpanDepth last = stack.getLast();
                    final long beforeStartTime = getStartTime(last.getSpanAlign());
                    final SpanDepth spanDepth = new SpanDepth(spanAlign, getNextId(), beforeStartTime);
                    stack.push(spanDepth);
                } else {
                    if (parentDepth > currentDepth) {
                        // pop if parentDepth is larger
                        // difference in depth may be greater than 1, so pop and check the depth repeatedly until appropriate
                        SpanDepth lastPopSpanDepth;
                        while (true) {
                            lastPopSpanDepth = stack.pop();
                            SpanDepth popLast = stack.getLast();
                            if (popLast.getSpanAlign().getDepth() < currentDepth) {
                                break;
                            }
                        }
                        final long beforeLastEndTime = getLastTime(lastPopSpanDepth.getSpanAlign());
                        stack.push(new SpanDepth(spanAlign, getNextId(), beforeLastEndTime));
                    } else {
                        // throw away the object right infront if it has the same depth
                        final SpanDepth before = stack.pop();
                        final long beforeLastEndTime = getLastTime(before.getSpanAlign());
                        stack.push(new SpanDepth(spanAlign, getNextId(), beforeLastEndTime));
                    }
                }
            }

            if (spanAlign.isSpan()) {
                SpanBo spanBo = spanAlign.getSpanBo();
                final long begin = spanBo.getStartTime();
                final long elapsed = spanBo.getElapsed();
                final int spanBoSequence = stack.getLast().getId();
                int parentSequence;
                final SpanDepth parent = stack.getParent();
                if (parent == null) {
                    // root span
                    parentSequence = 0;
                } else {
                    parentSequence = parent.getId();
                }
            } else {
                SpanEventBo spanEventBo = spanAlign.getSpanEventBo();
                SpanBo spanBo = spanAlign.getSpanBo();

                final int spanBoEventSequence = stack.getLast().getId();
                final SpanDepth parent = stack.getParent();
                if (parent == null) {
                    throw new IllegalStateException("parent is null. stack:" + stack);
                }
                final int parentSequence = parent.getId();
            }
        }
    }

    private int getNextId() {
        return idGen++;
    }

    private long getLastTime(SpanAlign spanAlign) {
        final SpanBo spanBo = spanAlign.getSpanBo();
        if (spanAlign.isSpan()) {
            return spanBo.getStartTime() + spanBo.getElapsed();
        } else {
            SpanEventBo spanEventBo = spanAlign.getSpanEventBo();
            return spanBo.getStartTime() + spanEventBo.getStartElapsed() + spanEventBo.getEndElapsed();
        }
    }

    private long getStartTime(SpanAlign spanAlign) {
        final SpanBo spanBo = spanAlign.getSpanBo();
        if (spanAlign.isSpan()) {
            return spanBo.getStartTime();
        } else {
            return spanBo.getStartTime() + spanAlign.getSpanEventBo().getStartElapsed();
        }
    }

    private SpanEventBo makeSpanEventBo(final boolean async, final short sequence, final int depth) {
        SpanEventBo event = new SpanEventBo();
        event.setAsyncId(async ? 1 : -1);
        event.setSequence(sequence);
        event.setDepth(depth);

        return event;
    }
}
