package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.web.service.NodeId;

import java.util.*;

/**
 * @author emeroad
 */
public class ApplicationRelationList {
    private final Map<NodeId, ApplicationRelation> linkMap = new HashMap<NodeId, ApplicationRelation>();

    public List<ApplicationRelation> getLinks() {
        return new ArrayList<ApplicationRelation>(this.linkMap.values());
    }

    public void addRelation(ApplicationRelation relation) {
        if (relation == null) {
            throw new NullPointerException("relation must not be null");
        }

        final NodeId id = relation.getId();
        final ApplicationRelation find = this.linkMap.get(id);
        if (find != null) {
            find.add(relation);
        } else {
            this.linkMap.put(id, relation);
        }
    }
}
