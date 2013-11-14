package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.web.service.NodeId;

import java.util.*;

/**
 * @author emeroad
 */
public class ApplicationList {

    private final Map<NodeId, Application> nodeMap = new HashMap<NodeId, Application>();

    public List<Application> getNodeList() {
        return new ArrayList<Application>(this.nodeMap.values());
    }

    public void markSequence() {
        int index = 0;
        for (Application application : this.nodeMap.values()) {
            application.setSequence(index++);
        }
    }

    public Application find(NodeId applicationId) {
        if (applicationId == null) {
            throw new NullPointerException("applicationId must not be null");
        }
        return this.nodeMap.get(applicationId);
    }

    public void addApplication(Application application) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        final NodeId id = application.getId();
        final Application find = nodeMap.get(id);
        if (find != null) {
            find.add(application);
        } else {
            nodeMap.put(id, application);
        }
    }

    public void build() {
        for (Application application: nodeMap.values()) {
            application.build();
        }
    }
}
