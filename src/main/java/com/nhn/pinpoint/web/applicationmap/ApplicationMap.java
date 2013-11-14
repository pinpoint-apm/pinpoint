package com.nhn.pinpoint.web.applicationmap;

import java.util.*;

import com.nhn.pinpoint.web.service.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.web.vo.TimeSeriesStore;

/**
 * Application map
 * 
 * @author netspider
 * @author emeroad
 */
public class ApplicationMap {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ApplicationList applications = new ApplicationList();
    private final ApplicationRelationList relations = new ApplicationRelationList();

	private final Set<String> applicationNames = new HashSet<String>();

	private TimeSeriesStore timeSeriesStore;
	
	ApplicationMap() {
	}


	public List<Application> getNodes() {
		return this.applications.getNodeList();
	}

	public List<ApplicationRelation> getLinks() {
		return this.relations.getLinks();
	}

	void indexingApplication() {
        this.applications.markSequence();
	}

	Application findApplication(NodeId applicationId) {
        return this.applications.find(applicationId);
	}

    void addApplication(List<Application> applicationList) {
        for (Application application : applicationList) {
            this.addApplication(application);
        }
    }

	void addApplication(Application application) {
		if (!application.getServiceType().isRpcClient()) {
			applicationNames.add(application.getApplicationName());
		}
        applications.addApplication(application);
	}

    void addRelation(List<ApplicationRelation> relationList) {
        for (ApplicationRelation applicationRelation : relationList) {
            relations.addRelation(applicationRelation);
        }
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
