package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.web.vo.Application;

import java.util.*;

/**
 * @author emeroad
 */
public class NodeList {
    // 정확하게 이름으로만 확인을 해야 하는지 확인후 Application으로 해도 될경우 삭제가 필요함.
    @Deprecated
    private Set<String> applicationNameSet = new HashSet<String>();

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
        applicationNameSet.add(nodeId.getName());
    }


    public void addNodeList(List<Node> sourceList) {
        for (Node source : sourceList) {
            addNode(source);
        }
    }

    public boolean containsNode(String applicationName) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        return applicationNameSet.contains(applicationName);
    }
}
