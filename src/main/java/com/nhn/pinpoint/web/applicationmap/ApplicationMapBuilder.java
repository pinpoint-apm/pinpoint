package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.rawdata.HostList;
import com.nhn.pinpoint.web.applicationmap.rawdata.RawStatisticsData;
import com.nhn.pinpoint.web.applicationmap.rawdata.TransactionFlowStatistics;
import com.nhn.pinpoint.web.service.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author emeroad
 */
public class ApplicationMapBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ApplicationMapBuilder() {
    }

    public ApplicationMap build(Set<TransactionFlowStatistics> transactionFlowStatistics) {
        if (transactionFlowStatistics == null) {
            throw new NullPointerException("rawData must not be null");
        }
        final RawStatisticsData rawData = new RawStatisticsData(transactionFlowStatistics);

        final ApplicationMap nodeMap = new ApplicationMap();

        // extract agent
        Map<NodeId, Set<AgentInfoBo>> agentMap = rawData.getAgentMap();
        // 변경하면 안됨
        final List<Application> sourceNode = createSourceApplication(rawData, agentMap);
        nodeMap.addApplication(sourceNode);


        // indexing application (UI의 서버맵을 그릴 때 key 정보가 필요한데 unique해야하고 link정보와 맞춰야 됨.)
        nodeMap.indexingApplication();
        // 변경하면 안됨.
        List<ApplicationRelation> sourceLink = createSourceLink(rawData, nodeMap);
        nodeMap.addRelation(sourceLink);


        nodeMap.buildApplication();

        return nodeMap;
    }

    private List<ApplicationRelation> createSourceLink(RawStatisticsData rawData, ApplicationMap nodeMap) {
        final List<ApplicationRelation> result = new ArrayList<ApplicationRelation>();
        // extract relation
        for (TransactionFlowStatistics stat : rawData.getRawData()) {
            final NodeId fromApplicationId = stat.getFromApplicationId();
            Application from = nodeMap.findApplication(fromApplicationId);
            // TODO
            final NodeId toApplicationId = stat.getToApplicationId();
            Application to = nodeMap.findApplication(toApplicationId);

            // rpc client가 빠진경우임.
            if (to == null) {
                continue;
            }

            // RPC client인 경우 dest application이 이미 있으면 삭제, 없으면 unknown cloud로 변경.
            HostList toHostList = stat.getToHostList();
            ApplicationRelation link = new ApplicationRelation(from, to, toHostList);
            if (to.getServiceType().isRpcClient()) {
                if (!nodeMap.containsApplicationName(to.getApplicationName())) {
                    result.add(link);
                }
            } else {
                result.add(link);
            }
        }
        return result;
    }

    private List<Application> createSourceApplication(RawStatisticsData rawData, Map<NodeId, Set<AgentInfoBo>> agentMap) {
        final List<Application> result = new ArrayList<Application>();
        // extract application and histogram
        for (TransactionFlowStatistics stat : rawData.getRawData()) {
            // FROM -> TO에서 FROM이 CLIENT가 아니면 FROM은 application
            if (!stat.getFromServiceType().isRpcClient()) {
                final NodeId id = stat.getFromApplicationId();
                final Set<AgentInfoBo> agentSet = agentMap.get(id);
                // FIXME from은 tohostlist를 보관하지 않아서 없음. null로 입력. 그렇지 않으면 이상해짐 ㅡㅡ;
                Application application = new Application(id, stat.getFrom(), stat.getFromServiceType(), agentSet);
                result.add(application);
            }

            // FROM -> TO에서 TO가 CLIENT가 아니면 TO는 application
            if (!stat.getToServiceType().isRpcClient()) {
                final NodeId to = stat.getToApplicationId();

                Application application = new Application(to, stat.getTo(), stat.getToServiceType(), stat.getToHostList());
                result.add(application);
            }
        }
        return result;
    }

}
