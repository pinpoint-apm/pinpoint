package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;


public class SpanCallTreeTest {

    @Test
    public void hasFocusSpan1() {
        SpanBo root = new SpanBo();
        root.setAgentId(AgentId.of("root"));
        Align rootAlign = new SpanAlign(root);

        SpanCallTree callTreeNodes = new SpanCallTree(rootAlign);

        Predicate<SpanBo> spanBoPredicate = SpanFilters.agentIdFilter("root");
        Assertions.assertTrue(callTreeNodes.filterSpan(spanBoPredicate));
    }

    @Test
    public void hasFocusSpan2() {
        SpanBo root = new SpanBo();
        root.setAgentId(AgentId.of("root"));
        Align rootAlign = new SpanAlign(root);

        SpanCallTree callTreeNodes = new SpanCallTree(rootAlign);

        Predicate<SpanBo> spanBoPredicate = SpanFilters.agentIdFilter("unknown");
        Assertions.assertFalse(callTreeNodes.filterSpan(spanBoPredicate));
    }

    @Test
    public void hasFocusSpan_child_travel() {
        SpanCallTree rootCallTreeNodes = childTree("root", "child1", "child2");

        Predicate<SpanBo> spanBoPredicate = SpanFilters.agentIdFilter("root");
        Assertions.assertTrue(rootCallTreeNodes.filterSpan(spanBoPredicate));

        Predicate<SpanBo> spanBoPredicate1 = SpanFilters.agentIdFilter("child1");
        Assertions.assertTrue(rootCallTreeNodes.filterSpan(spanBoPredicate1));

        Predicate<SpanBo> spanBoPredicate2 = SpanFilters.agentIdFilter("child2");
        Assertions.assertTrue(rootCallTreeNodes.filterSpan(spanBoPredicate2));
    }

    @Test
    public void hasFocusSpan_child_travel_not_found() {
        SpanCallTree rootCallTreeNodes = childTree("root", "child2", "child3");

        Predicate<SpanBo> spanBoPredicate = SpanFilters.agentIdFilter("xxx");
        Assertions.assertFalse(rootCallTreeNodes.filterSpan(spanBoPredicate));
    }

    private SpanCallTree childTree(String parentAgentId, String childAgentId1, String childAgentId2) {
        SpanBo root = new SpanBo();
//        root.setSpanId(100);
        root.setAgentId(AgentId.of(parentAgentId));
        Align rootAlign = new SpanAlign(root);
        SpanCallTree rootCallTreeNodes = new SpanCallTree(rootAlign);

        SpanBo childSpan1 = new SpanBo();
//        childSpan1.setParentSpanId(100);
//        childSpan1.setSpanId(200);
        childSpan1.setAgentId(AgentId.of(childAgentId1));
        Align childAlign1 = new SpanAlign(childSpan1);
        SpanCallTree childCallTreeNodes1 = new SpanCallTree(childAlign1);
        rootCallTreeNodes.add(childCallTreeNodes1);

        SpanBo childSpan2 = new SpanBo();
//        childSpan2.setParentSpanId(200);
//        childSpan2.setSpanId(300);
        childSpan2.setAgentId(AgentId.of(childAgentId2));
        Align childAlign2 = new SpanAlign(childSpan2);
        SpanCallTree childCallTreeNodes2 = new SpanCallTree(childAlign2);
        rootCallTreeNodes.add(childCallTreeNodes2);

        return rootCallTreeNodes;
    }
}