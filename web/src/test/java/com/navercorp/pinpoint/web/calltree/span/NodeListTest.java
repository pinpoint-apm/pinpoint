package com.navercorp.pinpoint.web.calltree.span;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

class NodeListTest {

    @Test
    void remove() {
        Node node = mock(Node.class);

        List<Node> nodeList = new ArrayList<>();
        nodeList.add(node);

        NodeList nodes = NodeList.of(nodeList);
        nodes.remove(node);

        Assertions.assertTrue(nodes.isEmpty());
    }
}