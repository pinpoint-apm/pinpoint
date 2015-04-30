package com.navercorp.pinpoint.web.calltree.span;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.navercorp.pinpoint.common.bo.SpanEventBo;

public class CallTreeTest {

    private static final boolean SYNC = false;
    private static final boolean ASYNC = true;

    private List<String> expectResult = new ArrayList<String>();

    @Test
    public void add() {
        CallTree callTree = new CallTree(null);

        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");
        expectResult.add("#####");

        callTree.add(1, null);
        callTree.add(2, null);
        callTree.add(3, null);
        callTree.add(4, null);
        assertDepth("add", callTree, expectResult);
    }

    @Test
    public void addLevel() {
        CallTree callTree = new CallTree(null);

        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");
        expectResult.add("#####");
        expectResult.add("#####");
        expectResult.add("#####");
        expectResult.add("#####");

        callTree.add(1, null);
        callTree.add(2, null);
        callTree.add(3, null);
        callTree.add(4, null);
        callTree.add(-1, null);
        callTree.add(-1, null);
        callTree.add(-1, null);
        assertDepth("addLevel", callTree, expectResult);
    }

    @Test
    public void addComplex() {
        CallTree callTree = new CallTree(null);

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

        callTree.add(1, null);
        callTree.add(2, null);
        callTree.add(3, null);
        callTree.add(4, null);
        callTree.add(5, null);
        callTree.add(-1, null);
        callTree.add(-1, null);
        callTree.add(-1, null);
        callTree.add(-1, null);
        callTree.add(4, null);
        callTree.add(-1, null);
        callTree.add(5, null);
        callTree.add(6, null);
        callTree.add(7, null);
        callTree.add(-1, null);
        callTree.add(4, null);
        callTree.add(-1, null);
        callTree.add(-1, null);
        assertDepth("addComplex", callTree, expectResult);
    }

    @Test
    public void addSubTree() {
        CallTree callTree = new CallTree(null);

        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("##");

        callTree.add(1, null);
        CallTree subTree = new CallTree(null);
        callTree.add(subTree);
        callTree.add(-1, null);
        assertDepth("addSubTree", callTree, expectResult);
    }

    @Test
    public void addNestedSubTree() {
        CallTree callTree = new CallTree(null);

        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");
        expectResult.add("#####");

        callTree.add(1, null);
        CallTree subTree = new CallTree(null);
        subTree.add(1, null);

        CallTree subTree2 = new CallTree(null);
        subTree.add(subTree2);
        callTree.add(subTree);
        assertDepth("addNestedSubTree", callTree, expectResult);
    }

    //
    // @Ignore
    // @Test
    // public void missing() {
    // expectResult.clear();
    // expectResult.add("#");
    //
    // callTree.clear();
    // callTree.add(makeSpanEventBo(SYNC, (short) 20, -1));
    // callTree.add(makeSpanEventBo(SYNC, (short) 21, -1));
    // callTree.add(makeSpanEventBo(SYNC, (short) 22, -1));
    // callTree.add(makeSpanEventBo(SYNC, (short) 23, -1));
    // assertDepth("missing-case-1", callTree, expectResult);
    //
    // expectResult.clear();
    // expectResult.add("#");
    // expectResult.add("##");
    // expectResult.add("###");
    // expectResult.add("###");
    //
    // callTree.clear();
    // callTree.add(makeSpanEventBo(SYNC, (short) 0, 1));
    // callTree.add(makeSpanEventBo(SYNC, (short) 1, 2));
    // callTree.add(makeSpanEventBo(SYNC, (short) 2, 3));
    // // callTree.add(makeSpanEventBo(SYNC, (short) 3, 4));
    // // callTree.add(makeSpanEventBo(SYNC, (short) 4, 5));
    // // callTree.add(makeSpanEventBo(SYNC, (short) 5, -1));
    // callTree.add(makeSpanEventBo(SYNC, (short) 6, -1));
    // callTree.add(makeSpanEventBo(SYNC, (short) 7, -1));
    // callTree.add(makeSpanEventBo(SYNC, (short) 8, -1));
    // callTree.add(makeSpanEventBo(SYNC, (short) 9, 4));
    // callTree.add(makeSpanEventBo(SYNC, (short) 10, -1));
    // callTree.add(makeSpanEventBo(SYNC, (short) 11, 5));
    // callTree.add(makeSpanEventBo(SYNC, (short) 12, 6));
    // callTree.add(makeSpanEventBo(SYNC, (short) 13, 7));
    // callTree.add(makeSpanEventBo(SYNC, (short) 14, -1));
    // callTree.add(makeSpanEventBo(SYNC, (short) 15, 4));
    // callTree.add(makeSpanEventBo(SYNC, (short) 16, -1));
    // callTree.add(makeSpanEventBo(SYNC, (short) 17, -1));
    // assertDepth("missing-case-2", callTree, expectResult);
    //
    // expectResult.clear();
    // expectResult.add("#");
    // expectResult.add("##");
    //
    // callTree.clear();
    // callTree.add(makeSpanEventBo(SYNC, (short) 0, 1, 1, -1));
    // // callTree.add(makeSpanEventBo(ASYNC, (short) 0, 1));
    // callTree.add(makeSpanEventBo(ASYNC, (short) 1, 2, 2, 1));
    // callTree.add(makeSpanEventBo(ASYNC, (short) 0, 1, -1, 2));
    // assertDepth("missing-case-3", callTree, expectResult);
    //
    // expectResult.clear();
    // expectResult.add("#");
    // expectResult.add("#");
    //
    // callTree.clear();
    // callTree.add(makeSpanEventBo(SYNC, (short) 0, 1, 1, -1));
    // // callTree.add(makeSpanEventBo(ASYNC, (short) 0, 1));
    // callTree.add(makeSpanEventBo(SYNC, (short) 1, -1));
    // assertDepth("missing-case-5", callTree, expectResult);
    //
    // }

    private void assertDepth(final String name, CallTree tree, List<String> result) {
        System.out.println("===== " + name + "=====");
        int index = 0;
        CallTreeCursor cursor = tree.getCursor();
        CallTreeNode node = null;
        while ((node = cursor.next()) != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i <= node.getDepth(); i++) {
                sb.append("#");
            }
            final String expected = result.get(index++);
            printDepth(index, sb.toString(), expected);
        }
        System.out.println("");
    }

    private void printDepth(final int index, final String depth, final String expected) {
        if (expected.equals(depth)) {
            System.out.println("Matched(" + index + "): " + depth);
        } else {
            System.out.println("Not Matched(" + index + "): " + depth + ", expected=" + expected);
        }
    }

    private SpanEventBo makeSpanEventBo(final boolean async, final short sequence, final int depth) {
        return makeSpanEventBo(async, sequence, depth, -1, -1);
    }

    private SpanEventBo makeSpanEventBo(final boolean async, final short sequence, final int depth, final int nextAsyncId, final int asyncId) {
        SpanEventBo event = new SpanEventBo();
        event.setAsyncId(async ? 1 : -1);
        event.setSequence(sequence);
        event.setDepth(depth);
        event.setNextAsyncId(nextAsyncId);
        event.setAsyncId(asyncId);

        return event;
    }
}