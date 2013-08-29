package com.nhn.pinpoint.web.applicationmap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.vo.TimeseriesResponses;

/**
 * Application map
 * 
 * @author netspider
 */
public class ApplicationMap {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private boolean built = false;

	private final Set<TransactionFlowStatistics> data;
	private final Set<String> applicationNames = new HashSet<String>();
	private final Map<String, Application> applications = new HashMap<String, Application>();
	private final Map<String, ApplicationRelation> relations = new HashMap<String, ApplicationRelation>();

	private TimeseriesResponses timeseriesResponses;
	
	public ApplicationMap(Set<TransactionFlowStatistics> data) {
		this.data = data;
	}

	public Collection<Application> getNodes() {
		if (!built) {
			throw new IllegalStateException("Map was not built yet.");
		}
		return this.applications.values();
	}

	public Collection<ApplicationRelation> getLinks() {
		if (!built) {
			throw new IllegalStateException("Map was not built yet.");
		}
		return this.relations.values();
	}

	public ApplicationMap build() {
		if (built)
			return this;

		// extract application
		for (TransactionFlowStatistics stat : data) {
			if (!stat.getFromServiceType().isRpcClient()) {
				addApplication(new Application(makeApplicationId(stat.getFrom(), stat.getFromServiceType()), stat.getFrom(), stat.getFromServiceType(), null));
			}
			if (!stat.getToServiceType().isRpcClient()) {
				addApplication(new Application(makeApplicationId(stat.getTo(), stat.getToServiceType()), stat.getTo(), stat.getToServiceType(), stat.getToHostList()));
			}
			if (!applicationNames.contains(stat.getTo())) {
				addApplication(new Application(makeApplicationId(stat.getTo(), stat.getToServiceType()), stat.getTo(), stat.getToServiceType(), stat.getToHostList()));
			}
		}

		// indexing application
		int index = 0;
		for (Entry<String, Application> entry : applications.entrySet()) {
			entry.getValue().setSequence(index++);
		}

		// extract relation
		for (TransactionFlowStatistics stat : data) {
			Application from = findApplication(stat.getFrom(), stat.getFromServiceType());
			Application to = findApplication(stat.getTo(), stat.getToServiceType());

			// rpc client가 빠진경우임.
			if (to == null) {
				continue;
			}

			// RPC client인 경우 dest application이 이미 있으면 삭제, 없으면 unknown cloud로
			// 변경.
			if (to.getServiceType().isRpcClient()) {
				if (!applicationNames.contains(to.getApplicationName())) {
					addRelation(new ApplicationRelation(from, to, stat.getToHostList()));
				}
			} else {
				addRelation(new ApplicationRelation(from, to, stat.getToHostList()));
			}
		}

		built = true;
		return this;
	}

	private String makeApplicationId(String applicationName, ServiceType serviceType) {
		return applicationName + serviceType;
	}

	private Application findApplication(String applicationName, ServiceType serviceType) {
		return applications.get(makeApplicationId(applicationName, serviceType));
	}

	private void addApplication(Application application) {
		if (!application.getServiceType().isRpcClient()) {
			applicationNames.add(application.getApplicationName());
		}

		if (applications.containsKey(application.getId())) {
			logger.debug("Merge application. {}", application);
			applications.get(application.getId()).mergeWith(application);
		} else {
			logger.debug("Add application. {}", application);
			applications.put(application.getId(), application);
		}
	}

	private void addRelation(ApplicationRelation relation) {
		if (relations.containsKey(relation.getId())) {
			logger.debug("Merge relation. {}", relation);
			relations.get(relation.getId()).mergeWith(relation);
		} else {
			logger.debug("Add relation. {}", relation);
			relations.put(relation.getId(), relation);
		}
	}

	public TimeseriesResponses getTimeseriesResponses() {
		return timeseriesResponses;
	}

	public void setTimeseriesResponses(TimeseriesResponses timeseriesResponses) {
		this.timeseriesResponses = timeseriesResponses;
	}
}
