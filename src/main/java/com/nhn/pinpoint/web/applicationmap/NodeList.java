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

    public Node findNode(Application application) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        return this.nodeMap.get(application);
    }

    public boolean addNode(Node node) {
        if (node == null) {
            throw new NullPointerException("node must not be null");
        }
        final Application nodeId = node.getApplication();
        Node findNode = findNode(nodeId);
        if (findNode != null) {
            return false;
        }
        return nodeMap.put(nodeId, node) == null;
    }


    public void addNodeList(Collection<Node> nodeList) {
        if (nodeList == null) {
            throw new NullPointerException("nodeList must not be null");
        }
        for (Node node : nodeList) {
            addNode(node);
        }
    }

    public boolean containsNode(Application application) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        return nodeMap.containsKey(application);
    }
}
