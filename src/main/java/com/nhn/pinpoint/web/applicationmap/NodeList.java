package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.web.vo.Application;

import java.util.*;

/**
 * @author emeroad
 */
public class NodeList {

    private final Map<Application, Node> nodeMap = new HashMap<Application, Node>();

    public List<Node> getNodeList() {
        return new ArrayList<Node>(this.nodeMap.values());
    }

    public void markSequence() {
        int index = 0;
        for (Node node : this.nodeMap.values()) {
            node.setSequence(index++);
        }
    }

    public Node find(Application applicationId) {
        if (applicationId == null) {
            throw new NullPointerException("applicationId must not be null");
        }
        return this.nodeMap.get(applicationId);
    }

    private void addApplication(Node source) {
        if (source == null) {
            throw new NullPointerException("source must not be null");
        }
        final Application id = source.getApplication();
        final Node find = nodeMap.get(id);
        if (find != null) {
            find.add(source);
        } else {
            final Node node = new Node(source);
            nodeMap.put(id, node);
        }
    }

    public void build() {
        for (Node node : nodeMap.values()) {
            node.build();
        }
    }


    public void buildApplication(List<Node> sourceList) {
        for (Node source : sourceList) {
            addApplication(source);
        }

    }
}
