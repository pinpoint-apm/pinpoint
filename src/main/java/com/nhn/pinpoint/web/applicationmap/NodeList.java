package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.web.vo.Application;

import java.util.*;

/**
 * @author emeroad
 */
public class NodeList {

    private final Map<Application, Node> nodeMap = new HashMap<Application, Node>();

    public Collection<Node> getNodeList() {
        return this.nodeMap.values();
    }

    public Node findNode(Application nodeId) {
        if (nodeId == null) {
            throw new NullPointerException("nodeId must not be null");
        }
        return this.nodeMap.get(nodeId);
    }

    private void addNode(Node newNode) {
        if (newNode == null) {
            throw new NullPointerException("newNode must not be null");
        }
        final Application nodeId = newNode.getApplication();
        Node node = findNode(nodeId);
        if (node != null) {
            return;
        }
        nodeMap.put(nodeId, newNode);
    }


    public void addNodeList(List<Node> sourceList) {
        for (Node source : sourceList) {
            addNode(source);
        }
    }
}
