package com.nhn.pinpoint.web.applicationmap;

import java.util.*;
import java.util.Map.Entry;

import com.nhn.pinpoint.web.service.ComplexNodeId;
import com.nhn.pinpoint.web.service.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.rawdata.RawStatisticsData;
import com.nhn.pinpoint.web.applicationmap.rawdata.TransactionFlowStatistics;
import com.nhn.pinpoint.web.util.MergeableHashMap;
import com.nhn.pinpoint.web.util.MergeableMap;
import com.nhn.pinpoint.web.vo.TimeSeriesStore;

/**
 * Application map
 * 
 * @author netspider
 */
public class ApplicationMap {

	private Logger logger = LoggerFactory.getLogger(this.getClass());


	private final MergeableMap<NodeId, Application> applications = new MergeableHashMap<NodeId, Application>();
	private final MergeableMap<NodeId, ApplicationRelation> relations = new MergeableHashMap<NodeId, ApplicationRelation>();
	private final Set<String> applicationNames = new HashSet<String>();

	private TimeSeriesStore timeSeriesStore;
	
	ApplicationMap() {
	}


	public Collection<Application> getNodes() {

		return this.applications.values();
	}

	public Collection<ApplicationRelation> getLinks() {
		return this.relations.values();
	}

	void indexingApplication() {
		int index = 0;
		for (Entry<NodeId, Application> entry : applications.entrySet()) {
			entry.getValue().setSequence(index++);
		}
	}

	Application findApplication(NodeId applicationId) {
		return applications.get(applicationId);
	}

    void addApplication(List<Application> applicationList) {
        for(Application application : applicationList) {
            this.addApplication(application);
        }
    }

	void addApplication(Application application) {
		if (!application.getServiceType().isRpcClient()) {
			applicationNames.add(application.getApplicationName());
		}
		applications.putOrMerge(application.getId(), application);
	}

    void addRelation(List<ApplicationRelation> relationList) {
        for (ApplicationRelation applicationRelation : relationList) {
            addRelation(applicationRelation);
        }
    }

	void addRelation(ApplicationRelation relation) {
		relations.putOrMerge(relation.getId(), relation);
	}

	public TimeSeriesStore getTimeSeriesStore() {
		return timeSeriesStore;
	}

	public void setTimeSeriesStore(TimeSeriesStore timeSeriesStore) {
		this.timeSeriesStore = timeSeriesStore;
	}

    public void buildApplication() {
        // build application
        for (Map.Entry<NodeId, Application> app : applications.entrySet()) {
            app.getValue().build();
        }
    }

    public boolean containsApplicationName(String applicationName) {
        return applicationNames.contains(applicationName);
    }
}
