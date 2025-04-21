package com.navercorp.pinpoint.web.applicationmap.nodes;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NodeListTest {

    @Test
    void addNode() {
        NodeList.Builder builder = NodeList.newBuilder();
        Assertions.assertTrue(builder.addNode(new Node(new Application("testApp1", ServiceType.UNKNOWN))));
        Assertions.assertTrue(builder.addNode(new Node(new Application("testApp2", ServiceType.UNKNOWN))));

        NodeList list = builder.build();

        Assertions.assertEquals(2, list.size());
    }

    @Test
    void addNode_sameApp() {
        NodeList.Builder builder = NodeList.newBuilder();
        Assertions.assertTrue(builder.addNode(new Node(new Application("testApp1", ServiceType.UNKNOWN))));
        Assertions.assertFalse(builder.addNode(new Node(new Application("testApp1", ServiceType.UNKNOWN))));

        NodeList list = builder.build();

        Assertions.assertEquals(1, list.size());
    }
}