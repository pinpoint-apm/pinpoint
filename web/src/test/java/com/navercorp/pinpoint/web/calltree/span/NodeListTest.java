package com.navercorp.pinpoint.web.calltree.span;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class NodeListTest {

    @Test
    void remove() {
        Node node = mock(Node.class);

        NodeList nodes = new NodeList();
        nodes.remove(node);

        Assertions.assertTrue(nodes.isEmpty());
    }
}