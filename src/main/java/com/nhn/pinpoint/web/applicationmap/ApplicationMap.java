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

    private final NodeList nodeList = new NodeList();
    private final LinkList linkList = new LinkList();

	private final Set<String> applicationNames = new HashSet<String>();

	private TimeSeriesStore timeSeriesStore;
	
	ApplicationMap() {
	}


	public List<Node> getNodes() {
		return this.nodeList.getNodeList();
	}

	public List<Link> getLinks() {
		return this.linkList.getLinks();
	}

	void indexingNode() {
        this.nodeList.markSequence();
	}

	Node findApplication(NodeId applicationId) {
        return this.nodeList.find(applicationId);
	}

    void addApplication(List<Node> nodeList) {
        for (Node node : nodeList) {
            this.addApplicationName(node);
        }
        this.nodeList.buildApplication(nodeList);
    }

	void addApplicationName(Node node) {
		if (!node.getServiceType().isRpcClient()) {
			applicationNames.add(node.getApplicationName());
		}

	}

    void addLink(List<Link> relationList) {
        linkList.buildRelation(relationList);
    }


	public TimeSeriesStore getTimeSeriesStore() {
		return timeSeriesStore;
	}

	public void setTimeSeriesStore(TimeSeriesStore timeSeriesStore) {
		this.timeSeriesStore = timeSeriesStore;
	}

    public void buildApplication() {
        this.nodeList.build();
    }

    public boolean containsApplicationName(String applicationName) {
        return applicationNames.contains(applicationName);
    }
}
