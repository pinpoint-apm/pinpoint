package com.nhn.pinpoint.web.applicationmap;

import java.util.*;

import com.nhn.pinpoint.web.service.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.web.vo.TimeSeriesStore;

/**
 * Node map
 * 
 * @author netspider
 * @author emeroad
 */
public class ApplicationMap {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final NodeList applications = new NodeList();
    private final LinkList relations = new LinkList();

	private final Set<String> applicationNames = new HashSet<String>();

	private TimeSeriesStore timeSeriesStore;
	
	ApplicationMap() {
	}


	public List<Node> getNodes() {
		return this.applications.getNodeList();
	}

	public List<Link> getLinks() {
		return this.relations.getLinks();
	}

	void indexingApplication() {
        this.applications.markSequence();
	}

	Node findApplication(NodeId applicationId) {
        return this.applications.find(applicationId);
	}

    void addApplication(List<Node> nodeList) {
        for (Node node : nodeList) {
            this.addApplicationName(node);
        }
        this.applications.buildApplication(nodeList);
    }

	void addApplicationName(Node node) {
		if (!node.getServiceType().isRpcClient()) {
			applicationNames.add(node.getApplicationName());
		}

	}

    void addRelation(List<Link> relationList) {
        relations.buildRelation(relationList);
    }


	public TimeSeriesStore getTimeSeriesStore() {
		return timeSeriesStore;
	}

	public void setTimeSeriesStore(TimeSeriesStore timeSeriesStore) {
		this.timeSeriesStore = timeSeriesStore;
	}

    public void buildApplication() {
        this.applications.build();
    }

    public boolean containsApplicationName(String applicationName) {
        return applicationNames.contains(applicationName);
    }
}
