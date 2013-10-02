package com.nhn.pinpoint.web.applicationmap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.rawdata.Host;
import com.nhn.pinpoint.web.applicationmap.rawdata.RawStatisticsData;
import com.nhn.pinpoint.web.applicationmap.rawdata.ResponseHistogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.TransactionFlowStatistics;
import com.nhn.pinpoint.web.util.MergeableHashMap;
import com.nhn.pinpoint.web.util.MergeableMap;
import com.nhn.pinpoint.web.vo.TimeseriesResponses;

/**
 * Application map
 * 
 * @author netspider
 */
public class ApplicationMap {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private boolean built = false;
	private final RawStatisticsData rawData;
	private final MergeableMap<String, Application> applications = new MergeableHashMap<String, Application>();
	private final MergeableMap<String, ApplicationRelation> relations = new MergeableHashMap<String, ApplicationRelation>();

	private final Set<String> applicationNames = new HashSet<String>();
	private TimeseriesResponses timeseriesResponses;

	public ApplicationMap(Set<TransactionFlowStatistics> rawData) {
		this.rawData = new RawStatisticsData(rawData);
	}

	public ApplicationMap build() {
		if (built)
			return this;

		// extract agent
		Map<String, Set<AgentInfoBo>> agentMap = rawData.getAgentMap();
		
		// extract application and histogram
		MergeableMap<String, ResponseHistogram> hostHistogramMap = new MergeableHashMap<String, ResponseHistogram>();
		for (TransactionFlowStatistics stat : rawData) {
			// FROM -> TO에서 FROM이 CLIENT가 아니면 FROM은 application
			if (!stat.getFromServiceType().isRpcClient()) {
				String id = stat.getFromApplicationId();
				Set<AgentInfoBo> agentSet = agentMap.get(id);
				// FIXME from은 tohostlist를 보관하지 않아서 없음. null로 입력. 그렇지 않으면 이상해짐 ㅡㅡ;
				addApplication(new Application(id, stat.getFrom(), stat.getFromServiceType(), null, agentSet));
			}
			
			// FROM -> TO에서 TO가 CLIENT가 아니면 TO는 application
			if (!stat.getToServiceType().isRpcClient()) {
				String id = stat.getToApplicationId();
				// application histogram map 생성.
				for (Entry<String, Host> entry : stat.getToHostList().entrySet()) {
					Host host = entry.getValue();
					ResponseHistogram histogram = host.getHistogram();
					ResponseHistogram value= new ResponseHistogram(histogram.getId(), histogram.getServiceType());
					hostHistogramMap.putOrMerge(value.getId(), value.mergeWith(histogram));
				}
				addApplication(new Application(id, stat.getTo(), stat.getToServiceType(), stat.getToHostList(), null));
			}
			
			// FIXME 이거 필요없을듯
//			if (!applicationNames.contains(stat.getTo())) {
//				Set<AgentInfoBo> agentSet = agentMap.get(stat.getFromApplicationId());
//				// application histogram map 생성.
//				for (Entry<String, Host> entry : stat.getToHostList().entrySet()) {
//					Host host = entry.getValue();
//					ResponseHistogram histogram = host.getHistogram();
//					ResponseHistogram value = new ResponseHistogram(histogram.getId(), histogram.getServiceType());
//					hostHistogramMap.putOrMerge(value.getId(), value.mergeWith(histogram));
//				}
//				addApplication(new Application(stat.getToApplicationId(), stat.getTo(), stat.getToServiceType(), stat.getToHostList(), agentSet));
//			}
		}
		
		for (Entry<String, Application> entry : applications.entrySet()) {
			Application application = entry.getValue();
			application.mapHistogram(hostHistogramMap);
		}
		
		// indexing application
		indexingApplication();

		// extract relation
		for (TransactionFlowStatistics stat : rawData) {
			Application from = findApplication(stat.getFromApplicationId());
			Application to = findApplication(stat.getToApplicationId());

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
		for (Entry<String, Application> entry : applications.entrySet()) {
			entry.getValue().setSequence(index++);
		}
	}

	private Application findApplication(String applicationId) {
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

	public TimeseriesResponses getTimeseriesResponses() {
		return timeseriesResponses;
	}

	public void setTimeseriesResponses(TimeseriesResponses timeseriesResponses) {
		this.timeseriesResponses = timeseriesResponses;
	}
}
