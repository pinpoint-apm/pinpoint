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

    public void buildRelation(List<ApplicationRelation> relationList) {
        for (ApplicationRelation applicationRelation : relationList) {
            buildRelation(applicationRelation);
        }
    }

    public void buildRelation(ApplicationRelation sourceLink) {
        if (sourceLink == null) {
            throw new NullPointerException("sourceLink must not be null");
        }

        final NodeId id = sourceLink.getId();
        final ApplicationRelation find = this.linkMap.get(id);
        if (find != null) {
            find.add(sourceLink);
        } else {
            ApplicationRelation copy = sourceLink.deepCopy();
            this.linkMap.put(id, copy);
        }
    }
}
