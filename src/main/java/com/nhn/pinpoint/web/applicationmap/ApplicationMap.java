package com.nhn.pinpoint.web.applicationmap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

	private boolean built = false;
	private final RawStatisticsData rawData;
	private final MergeableMap<NodeId, Application> applications = new MergeableHashMap<NodeId, Application>();
	private final MergeableMap<NodeId, ApplicationRelation> relations = new MergeableHashMap<NodeId, ApplicationRelation>();
	private final Set<String> applicationNames = new HashSet<String>();

	private TimeSeriesStore timeSeriesStore;
	
	public ApplicationMap(Set<TransactionFlowStatistics> rawData) {
		this.rawData = new RawStatisticsData(rawData);
		logger.debug("ApplicationMap rawdata={}", this.rawData);
	}

	public ApplicationMap build() {
		if (built)
			return this;

		// extract agent
		Map<NodeId, Set<AgentInfoBo>> agentMap = rawData.getAgentMap();
		
		// extract application and histogram
		for (TransactionFlowStatistics stat : rawData) {
			// FROM -> TO에서 FROM이 CLIENT가 아니면 FROM은 application
			if (!stat.getFromServiceType().isRpcClient()) {
//				String id = stat.getFromApplicationId();
                NodeId id = stat.getFromApplicationId();
				Set<AgentInfoBo> agentSet = agentMap.get(id);
				// FIXME from은 tohostlist를 보관하지 않아서 없음. null로 입력. 그렇지 않으면 이상해짐 ㅡㅡ;
                Application application = new Application(id, stat.getFrom(), stat.getFromServiceType(), null, agentSet);
                logger.info("add Application:{}", application);
                addApplication(application);
			}
			
			// FROM -> TO에서 TO가 CLIENT가 아니면 TO는 application
			if (!stat.getToServiceType().isRpcClient()) {
//				String id = stat.getToApplicationId();
                NodeId to = stat.getToApplicationId();
                NodeId from = stat.getFromApplicationId();


                Application application = new Application(to, stat.getTo(), stat.getToServiceType(), stat.getToHostList(), null);
                logger.info("add Application:{}", application);
                addApplication(application);
			}
		}
		
		// indexing application (UI의 서버맵을 그릴 때 key 정보가 필요한데 unique해야하고 link정보와 맞춰야 됨.)
		indexingApplication();

		// extract relation
		for (TransactionFlowStatistics stat : rawData) {
            NodeId fromApplicationId = stat.getFromApplicationId();
            Application from = findApplication(fromApplicationId);
            // TODO
            NodeId toApplicationId = stat.getToApplicationId();
            Application to = findApplication(toApplicationId);

			// rpc client가 빠진경우임.
			if (to == null) {
				continue;
			}

			// RPC client인 경우 dest application이 이미 있으면 삭제, 없으면 unknown cloud로 변경.
			if (to.getServiceType().isRpcClient()) {
				if (!applicationNames.contains(to.getApplicationName())) {
					addRelation(new ApplicationRelation(from, to, stat.getToHostList()));
				}
			} else {
                logger.info("from:{}, to:{}", from, to);
				addRelation(new ApplicationRelation(from, to, stat.getToHostList()));
			}
		}

		// build application
		for (Entry<NodeId, Application> app : applications.entrySet()) {
			app.getValue().build();
		}
		
		built = true;
		return this;
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

	private void indexingApplication() {
		int index = 0;
		for (Entry<NodeId, Application> entry : applications.entrySet()) {
			entry.getValue().setSequence(index++);
		}
	}

	private Application findApplication(NodeId applicationId) {
		return applications.get(applicationId);
	}

	private void addApplication(Application application) {
		if (!application.getServiceType().isRpcClient()) {
			applicationNames.add(application.getApplicationName());
		}
		applications.putOrMerge(application.getId(), application);
	}

	private void addRelation(ApplicationRelation relation) {
		relations.putOrMerge(relation.getId(), relation);
	}

	public TimeSeriesStore getTimeSeriesStore() {
		return timeSeriesStore;
	}

	public void setTimeSeriesStore(TimeSeriesStore timeSeriesStore) {
		this.timeSeriesStore = timeSeriesStore;
	}
}
