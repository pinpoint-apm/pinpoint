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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;

/**
 * 
 * @author jaehong.kim
 *
 */
public class CallTreeTest {

    private static final boolean SYNC = false;
    private static final boolean ASYNC = true;

    private SpanCallTree callTree = new SpanCallTree(makeSpanAlign());
    private List<String> expectResult = new ArrayList<String>();

    @Test
    public void add() {
        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");
        expectResult.add("#####");

        callTree.add(1, makeSpanAlign(SYNC, (short) 0));
        callTree.add(2, makeSpanAlign(SYNC, (short) 1));
        callTree.add(3, makeSpanAlign(SYNC, (short) 2));
        callTree.add(4, makeSpanAlign(SYNC, (short) 3));
        assertDepth("add", callTree, expectResult);
    }

    @Test
    public void addAndSort() {
        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");
        expectResult.add("#####");

        callTree.add(1, makeSpanAlign(SYNC, (short) 0));
        callTree.add(2, makeSpanAlign(SYNC, (short) 1));
        callTree.add(3, makeSpanAlign(SYNC, (short) 2));
        callTree.add(4, makeSpanAlign(SYNC, (short) 3));
        assertDepth("addAndSort", callTree, expectResult);

        callTree.sort();
        assertDepth("addAndSort", callTree, expectResult);
    }

    @Test
    public void addLevel() {
        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");
        expectResult.add("#####");
        expectResult.add("#####");
        expectResult.add("#####");
        expectResult.add("#####");

        callTree.add(1, makeSpanAlign(SYNC, (short) 0));
        callTree.add(2, makeSpanAlign(SYNC, (short) 1));
        callTree.add(3, makeSpanAlign(SYNC, (short) 2));
        callTree.add(4, makeSpanAlign(SYNC, (short) 3));
        callTree.add(-1, makeSpanAlign(SYNC, (short) 4));
        callTree.add(-1, makeSpanAlign(SYNC, (short) 5));
        callTree.add(-1, makeSpanAlign(SYNC, (short) 6));
        assertDepth("addLevel", callTree, expectResult);
    }

    @Test
    public void addComplex() {
        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");
        expectResult.add("#####");
        expectResult.add("######");
        expectResult.add("######");
        expectResult.add("######");
        expectResult.add("######");
        expectResult.add("######");
        expectResult.add("#####");
        expectResult.add("#####");
        expectResult.add("######");
        expectResult.add("#######");
        expectResult.add("########");
        expectResult.add("########");
        expectResult.add("#####");
        expectResult.add("#####");
        expectResult.add("#####");

        callTree.add(1, makeSpanAlign(SYNC, (short) 0));
        callTree.add(2, makeSpanAlign(SYNC, (short) 1));
        callTree.add(3, makeSpanAlign(SYNC, (short) 2));
        callTree.add(4, makeSpanAlign(SYNC, (short) 3));
        callTree.add(5, makeSpanAlign(SYNC, (short) 4));
        callTree.add(-1, makeSpanAlign(SYNC, (short) 5));
        callTree.add(-1, makeSpanAlign(SYNC, (short) 6));
        callTree.add(-1, makeSpanAlign(SYNC, (short) 7));
        callTree.add(-1, makeSpanAlign(SYNC, (short) 8));
        callTree.add(4, makeSpanAlign(SYNC, (short) 9));
        callTree.add(-1, makeSpanAlign(SYNC, (short) 10));
        callTree.add(5, makeSpanAlign(SYNC, (short) 11));
        callTree.add(6, makeSpanAlign(SYNC, (short) 12));
        callTree.add(7, makeSpanAlign(SYNC, (short) 13));
        callTree.add(-1, makeSpanAlign(SYNC, (short) 14));
        callTree.add(4, makeSpanAlign(SYNC, (short) 15));
        callTree.add(-1, makeSpanAlign(SYNC, (short) 16));
        callTree.add(-1, makeSpanAlign(SYNC, (short) 17));
        assertDepth("addComplex", callTree, expectResult);
    }

    @Test
    public void addSubTree() {
        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("##");

        callTree.add(1, makeSpanAlign(SYNC, (short) 0));
        SpanCallTree subTree = new SpanCallTree(makeSpanAlign());
        callTree.add(subTree);
        callTree.add(-1, makeSpanAlign(SYNC, (short) 1));
        assertDepth("addSubTree", callTree, expectResult);
    }

    @Test
    public void addNestedSubTree() {
        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");
        expectResult.add("#####");

        callTree.add(1, makeSpanAlign(SYNC, (short) 0));
        SpanCallTree subTree = new SpanCallTree(makeSpanAlign(ASYNC, (short) 0));
        subTree.add(1, makeSpanAlign(ASYNC, (short) 1));

        SpanCallTree subTree2 = new SpanCallTree(makeSpanAlign(ASYNC, (short) 0));
        subTree.add(subTree2);
        callTree.add(subTree);
        assertDepth("addNestedSubTree", callTree, expectResult);
    }

    @Test
    public void missing() {
        expectResult.clear();
        expectResult.add("#");

        try {
            callTree.add(-1, makeSpanAlign(SYNC, (short) 20));
            callTree.add(-1, makeSpanAlign(SYNC, (short) 21));
            callTree.add(-1, makeSpanAlign(SYNC, (short) 22));
            callTree.add(-1, makeSpanAlign(SYNC, (short) 23));
        } catch (Exception ignored) {
        }
        assertDepth("missing-case-1", callTree, expectResult);
    }

    @Test
    public void missingMiddleNodes() {
        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");

        try {
            callTree.add(1, makeSpanAlign(SYNC, (short) 0));
            callTree.add(2, makeSpanAlign(SYNC, (short) 1));
            callTree.add(3, makeSpanAlign(SYNC, (short) 2));
            // callTree.add(4, makeSpanAlign(SYNC, (short) 3));
            // callTree.add(5, makeSpanAlign(SYNC, (short) 4));
            // callTree.add(-1, makeSpanAlign(SYNC, (short) 5));
            callTree.add(-1, makeSpanAlign(SYNC, (short) 6));
            callTree.add(-1, makeSpanAlign(SYNC, (short) 7));
            callTree.add(-1, makeSpanAlign(SYNC, (short) 8));
            callTree.add(4, makeSpanAlign(SYNC, (short) 9));
            callTree.add(-1, makeSpanAlign(SYNC, (short) 10));
            callTree.add(5, makeSpanAlign(SYNC, (short) 11));
            callTree.add(6, makeSpanAlign(SYNC, (short) 12));
            callTree.add(7, makeSpanAlign(SYNC, (short) 13));
            callTree.add(-1, makeSpanAlign(SYNC, (short) 14));
            callTree.add(4, makeSpanAlign(SYNC, (short) 15));
            callTree.add(-1, makeSpanAlign(SYNC, (short) 16));
            callTree.add(-1, makeSpanAlign(SYNC, (short) 17));
        } catch (Exception ignored) {
        }
        assertDepth("missing-case-2", callTree, expectResult);
    }

    @Test
    public void missingAsync() {
        expectResult.add("#");
        expectResult.add("##");

        callTree.add(1, makeSpanAlign(SYNC, (short) 0, 1, -1));
        try {
            SpanCallTree subTree = new SpanCallTree(makeSpanAlign());
            // subTree.add(1, makeSpanAlign(ASYNC, (short) 0, -1, 1));
            subTree.add(2, makeSpanAlign(ASYNC, (short) 1, 2, 1));
            callTree.add(subTree);
        } catch (Exception ignored) {
        }
        assertDepth("missing-case-3", callTree, expectResult);
    }

    @Test
    public void missingAsyncEvent() {
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("##");

        callTree.add(1, makeSpanAlign(SYNC, (short) 0, 1, -1));
        // callTree.add(makeSpanEventBo(ASYNC, (short) 0, 1));
        callTree.add(-1, makeSpanAlign(SYNC, (short) 1, -1, -1));
        assertDepth("missing-case-5", callTree, expectResult);

    }

    @Test
    public void sort() {
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###"); // remote
        expectResult.add("####"); // remote
        expectResult.add("###");
        expectResult.add("###");
        expectResult.add("##");
        
        SpanAlign root = makeSpanAlign(0, 10);
        SpanCallTree callTree = new SpanCallTree(root);
        callTree.add(1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 0, -1, -1, 1, 1));

        SpanAlign remoteRoot = makeSpanAlign(4, 5);
        SpanCallTree subTree = new SpanCallTree(remoteRoot);
        subTree.add(1, makeSpanAlign(remoteRoot.getSpanBo(), SYNC, (short) 0, -1, -1, 1, 1));
        callTree.add(subTree);

        callTree.add(2, makeSpanAlign(root.getSpanBo(), SYNC, (short) 1, -1, -1, 2, 1));
        callTree.add(-1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 2, -1, -1, 3, 1));
        callTree.add(1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 3, -1, -1, 4, 1));


        assertDepth("before sort", callTree, expectResult);
        
        callTree.sort();
        
        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("###");
        expectResult.add("###"); // remote
        expectResult.add("####"); // remote
        expectResult.add("##");

        assertDepth("after sort", callTree, expectResult);
    }

    @Test
    public void sort2() {
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###"); // remote 1
        expectResult.add("####"); // remote 1
        expectResult.add("###"); // remote 2
        expectResult.add("###");

        SpanAlign root = makeSpanAlign(0, 10);
        SpanCallTree callTree = new SpanCallTree(root);
        callTree.add(1, makeSpanAlign(root.getSpanBo(), SYNC, (short) 0, -1, -1, 1, 1));

        SpanAlign remoteRoot1 = makeSpanAlign(4, 5);
        SpanCallTree subTree1 = new SpanCallTree(remoteRoot1);
        subTree1.add(1, makeSpanAlign(remoteRoot1.getSpanBo(), SYNC, (short) 0, -1, -1, 1, 1));
        callTree.add(subTree1);

        SpanAlign remoteRoot2 = makeSpanAlign(3, 4);
        SpanCallTree subTree2 = new SpanCallTree(remoteRoot2);
        callTree.add(subTree2);

        callTree.add(2, makeSpanAlign(root.getSpanBo(), SYNC, (short) 1, -1, -1, 2, 1));

        assertDepth("before sort", callTree, expectResult);

        callTree.sort();

        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("###"); // remote 2
        expectResult.add("###"); // remote 1
        expectResult.add("####"); // remote 1

        assertDepth("after sort", callTree, expectResult);
    }

    private void assertDepth(final String name, SpanCallTree tree, List<String> result) {
        int index = 0;
        CallTreeIterator iterator = tree.iterator();
        CallTreeNode node = null;
        while ((node = iterator.next()) != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i <= node.getDepth(); i++) {
                sb.append("#");
            }
            final String expected = result.get(index++);
            printDepth(name, index, sb.toString(), expected);
        }
    }

    private void printDepth(final String name, final int index, final String depth, final String expected) {
        if (!expected.equals(depth)) {
            fail("Not Matched " + name + "(" + index + "): " + depth + ", expected=" + expected);
        }
    }

    private SpanAlign makeSpanAlign() {
        return makeSpanAlign(0, 0);
    }

    private SpanAlign makeSpanAlign(long startTime, int elapsed) {
        SpanBo span = new SpanBo();
        span.setStartTime(startTime);
        span.setElapsed(elapsed);

        return new SpanAlign(span);
    }

    private SpanAlign makeSpanAlign(final boolean async, final short sequence) {
        return makeSpanAlign(async, sequence, -1, -1);
    }

    private SpanAlign makeSpanAlign(final boolean async, final short sequence, final int nextAsyncId, final int asyncId) {
        return makeSpanAlign(new SpanBo(), async, sequence, nextAsyncId, asyncId, -1, -1);
    }

    private SpanAlign makeSpanAlign(SpanBo span, final boolean async, final short sequence, int nextAsyncId, final int asyncId, int startElapsed, int endElapsed) {
        SpanEventBo event = new SpanEventBo();
        event.setAsyncId(async ? 1 : -1);
        event.setSequence(sequence);
        event.setNextAsyncId(nextAsyncId);
        event.setAsyncId(asyncId);
        event.setStartElapsed(startElapsed);
        event.setEndElapsed(endElapsed);

        return new SpanAlign(span, event);
    }
}