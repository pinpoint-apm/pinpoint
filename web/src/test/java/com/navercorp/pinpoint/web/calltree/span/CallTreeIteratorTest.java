/*
 * Copyright 2015 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.calltree.span;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.common.bo.SpanEventBo;

/**
 * 
 * @author jaehong.kim
 *
 */
public class CallTreeIteratorTest {
    private static final boolean SYNC = false;
    private static final boolean ASYNC = true;
    private static final long START_TIME = 1430983914531L;
    private static final int ELAPSED = 10;

    @Test
    public void depth() {
        SpanAlign root = makeSpanAlign(START_TIME, 240);
        CallTree callTree = new SpanCallTree(root);
        callTree.add(1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 0, 1, 1));
        callTree.add(2, makeSpanAlign(root.getSpanBo(), SYNC, (short) 1, 2, 1));
        callTree.add(3, makeSpanAlign(root.getSpanBo(), SYNC, (short) 2, 3, 1));
        callTree.add(4, makeSpanAlign(root.getSpanBo(), SYNC, (short) 3, 4, 1));
        callTree.add(-1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 4, 5, 1));
        callTree.add(2, makeSpanAlign(root.getSpanBo(), SYNC, (short) 5, 6, 1));
        callTree.add(3, makeSpanAlign(root.getSpanBo(), SYNC, (short) 6, 7, 1));
        callTree.add(-1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 7, 8, 1));
        callTree.add(1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 8, 9, 1));

        CallTreeIterator iterator = callTree.iterator();
        // assertEquals(5, iterator.size());

        while (iterator.hasNext()) {
            CallTreeNode node = iterator.next();
            for (int i = 0; i <= node.getDepth(); i++) {
                System.out.print("#");
            }
            System.out.println("");
        }
    }

    @Test
    public void gap() {
        SpanAlign root = makeSpanAlign(START_TIME, 240);
        CallTree callTree = new SpanCallTree(root);
        callTree.add(1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 0, 1, 1));
        callTree.add(2, makeSpanAlign(root.getSpanBo(), SYNC, (short) 1, 2, 1));
        callTree.add(3, makeSpanAlign(root.getSpanBo(), SYNC, (short) 2, 3, 1));
        callTree.add(4, makeSpanAlign(root.getSpanBo(), SYNC, (short) 3, 4, 1));
        callTree.add(-1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 4, 5, 1));
        callTree.add(2, makeSpanAlign(root.getSpanBo(), SYNC, (short) 5, 6, 1));
        callTree.add(3, makeSpanAlign(root.getSpanBo(), SYNC, (short) 6, 7, 1));
        callTree.add(-1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 7, 8, 1));
        callTree.add(1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 8, 9, 1));

        CallTreeIterator iterator = callTree.iterator();
        // assertEquals(5, iterator.size());

        while (iterator.hasNext()) {
            CallTreeNode node = iterator.next();
            SpanAlign align = node.getValue();
            for (int i = 0; i <= align.getDepth(); i++) {
                System.out.print("#");
            }
            System.out.println(" : gap=" + align.getGap());
        }
    }
    
    @Ignore
    @Test
    public void gapAsync() {
        SpanAlign root = makeSpanAlign(START_TIME, 240);
        CallTree callTree = new SpanCallTree(root);
        callTree.add(1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 0, 1, 1));
        callTree.add(2, makeSpanAlign(root.getSpanBo(), SYNC, (short) 1, 2, 1));
        callTree.add(3, makeSpanAlign(root.getSpanBo(), SYNC, (short) 2, 3, 1));
        callTree.add(4, makeSpanAlign(root.getSpanBo(), SYNC, (short) 3, 4, 1, -1, 1));
        
        CallTree subTree = new SpanAsyncCallTree(root);
        subTree.add(1, makeSpanAlign(root.getSpanBo(), ASYNC, (short) 0, 5, 1, 1, -1));
        subTree.add(2, makeSpanAlign(root.getSpanBo(), ASYNC, (short) 1, 6, 1, 1, -1));
        subTree.add(3, makeSpanAlign(root.getSpanBo(), ASYNC, (short) 2, 7, 1, 1, -1));
        subTree.add(4, makeSpanAlign(root.getSpanBo(), ASYNC, (short) 3, 8, 1, 1, -1));
        callTree.add(subTree);
        
        callTree.add(5, makeSpanAlign(root.getSpanBo(), SYNC, (short) 4, 5, 1));
        callTree.add(2, makeSpanAlign(root.getSpanBo(), SYNC, (short) 5, 6, 1));
        callTree.add(3, makeSpanAlign(root.getSpanBo(), SYNC, (short) 6, 7, 1));
        callTree.add(-1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 7, 8, 1));
        callTree.add(1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 8, 9, 1));

        CallTreeIterator iterator = callTree.iterator();
        // assertEquals(5, iterator.size());

        System.out.println("gapAsync");
        while (iterator.hasNext()) {
            CallTreeNode node = iterator.next();
            SpanAlign align = node.getValue();
            for (int i = 0; i <= align.getDepth(); i++) {
                System.out.print("#");
            }
            System.out.println(" : gap=" + align.getGap());
        }
    }

    @Test
    public void gapComplex() {
        SpanAlign root = makeSpanAlign(START_TIME, 240);
        CallTree callTree = new SpanCallTree(root);
        callTree.add(1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 0, 1, 1));
        callTree.add(2, makeSpanAlign(root.getSpanBo(), SYNC, (short) 1, 2, 1));
        callTree.add(3, makeSpanAlign(root.getSpanBo(), SYNC, (short) 2, 3, 1));
        callTree.add(4, makeSpanAlign(root.getSpanBo(), SYNC, (short) 3, 4, 1, -1, 1));

        SpanAlign rpc = makeSpanAlign(START_TIME + 10, 240);
        CallTree rpcTree = new SpanCallTree(rpc);
        rpcTree.add(1, makeSpanAlign(rpc.getSpanBo(), SYNC, (short) 0, 1, 1));
        rpcTree.add(2, makeSpanAlign(rpc.getSpanBo(), SYNC, (short) 1, 2, 1));
        rpcTree.add(3, makeSpanAlign(rpc.getSpanBo(), SYNC, (short) 2, 3, 1));
        rpcTree.add(4, makeSpanAlign(rpc.getSpanBo(), SYNC, (short) 3, 4, 1));
        callTree.add(rpcTree);

        CallTree asyncTree = new SpanAsyncCallTree(root);
        asyncTree.add(1, makeSpanAlign(root.getSpanBo(), ASYNC, (short) 0, 5, 1, 1, -1));
        asyncTree.add(2, makeSpanAlign(root.getSpanBo(), ASYNC, (short) 1, 6, 1, 1, -1));
        asyncTree.add(3, makeSpanAlign(root.getSpanBo(), ASYNC, (short) 2, 7, 1, 1, -1));
        asyncTree.add(4, makeSpanAlign(root.getSpanBo(), ASYNC, (short) 3, 8, 1, 1, -1));
        callTree.add(asyncTree);
        
        callTree.add(5, makeSpanAlign(root.getSpanBo(), SYNC, (short) 4, 5, 1));
        callTree.add(2, makeSpanAlign(root.getSpanBo(), SYNC, (short) 5, 6, 1));
        callTree.add(3, makeSpanAlign(root.getSpanBo(), SYNC, (short) 6, 7, 1));
        callTree.add(-1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 7, 8, 1));
        callTree.add(1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 8, 9, 1));

        CallTreeIterator iterator = callTree.iterator();
        // assertEquals(5, iterator.size());

        System.out.println("gapComplex");
        while (iterator.hasNext()) {
            CallTreeNode node = iterator.next();
            SpanAlign align = node.getValue();
            for (int i = 0; i <= align.getDepth(); i++) {
                System.out.print("#");
            }
            System.out.println(" : gap=" + align.getGap());
            if(!node.isRoot()) {
                assertEquals(1, align.getGap());                
            }
        }
    }

    @Test
    public void executionTime() {
        SpanAlign root = makeSpanAlign(START_TIME, 10);
        CallTree callTree = new SpanCallTree(root);
        callTree.add(1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 0, 1, 8));
        callTree.add(2, makeSpanAlign(root.getSpanBo(), SYNC, (short) 1, 2, 4));
        callTree.add(3, makeSpanAlign(root.getSpanBo(), SYNC, (short) 2, 3, 3));
        callTree.add(4, makeSpanAlign(root.getSpanBo(), SYNC, (short) 3, 4, 2, -1, 1));

        SpanAlign rpc = makeSpanAlign(START_TIME + 10, 5);
        CallTree rpcTree = new SpanCallTree(rpc);
        rpcTree.add(1, makeSpanAlign(rpc.getSpanBo(), SYNC, (short) 0, 1, 4));
        rpcTree.add(2, makeSpanAlign(rpc.getSpanBo(), SYNC, (short) 1, 2, 3));
        rpcTree.add(3, makeSpanAlign(rpc.getSpanBo(), SYNC, (short) 2, 3, 2));
        rpcTree.add(4, makeSpanAlign(rpc.getSpanBo(), SYNC, (short) 3, 4, 1));
        callTree.add(rpcTree);

        CallTree asyncTree = new SpanAsyncCallTree(root);
        asyncTree.add(1, makeSpanAlign(root.getSpanBo(), ASYNC, (short) 0, 5, 4, 1, -1));
        asyncTree.add(2, makeSpanAlign(root.getSpanBo(), ASYNC, (short) 1, 6, 3, 1, -1));
        asyncTree.add(3, makeSpanAlign(root.getSpanBo(), ASYNC, (short) 2, 7, 2, 1, -1));
        asyncTree.add(4, makeSpanAlign(root.getSpanBo(), ASYNC, (short) 3, 8, 1, 1, -1));
        callTree.add(asyncTree);
        
        callTree.add(5, makeSpanAlign(root.getSpanBo(), SYNC, (short) 4, 5, 1));
        callTree.add(2, makeSpanAlign(root.getSpanBo(), SYNC, (short) 5, 6, 3));
        callTree.add(3, makeSpanAlign(root.getSpanBo(), SYNC, (short) 6, 7, 1));
        callTree.add(-1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 7, 8, 1));
        callTree.add(1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 8, 9, 1));

        CallTreeIterator iterator = callTree.iterator();
        // assertEquals(5, iterator.size());

        System.out.println("executionTime");
        while (iterator.hasNext()) {
            CallTreeNode node = iterator.next();
            SpanAlign align = node.getValue();
            for (int i = 0; i <= align.getDepth(); i++) {
                System.out.print("#");
            }
            System.out.println(" : executionTime=" + align.getExecutionMilliseconds());
            assertEquals(1, align.getExecutionMilliseconds());
        }
    }

    private SpanAlign makeSpanAlign(long startTime, int elapsed) {
        SpanBo span = new SpanBo();
        span.setStartTime(startTime);
        span.setElapsed(elapsed);

        return new SpanAlign(span);
    }

    private SpanAlign makeSpanAlign(SpanBo span, final boolean async, final short sequence, int startElapsed, int endElapsed) {
        return makeSpanAlign(span, async, sequence, startElapsed, endElapsed, -1, -1);
    }

    private SpanAlign makeSpanAlign(SpanBo span, final boolean async, final short sequence, int startElapsed, int endElapsed, final int asyncId, int nextAsyncId) {
        SpanEventBo event = new SpanEventBo();
        event.setSequence(sequence);
        event.setStartElapsed(startElapsed);
        event.setEndElapsed(endElapsed);
        event.setAsyncId(asyncId);
        event.setNextAsyncId(nextAsyncId);

        return new SpanAlign(span, event);
    }
}