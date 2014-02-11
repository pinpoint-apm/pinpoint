package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.rawdata.HostList;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatisticsData;
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

    public ApplicationMap build(Set<LinkStatistics> linkStatistics) {
        if (linkStatistics == null) {
            throw new NullPointerException("rawData must not be null");
        }
        final LinkStatisticsData linkStatisticsData = new LinkStatisticsData(linkStatistics);

        final ApplicationMap nodeMap = new ApplicationMap();

        // extract agent
        Map<NodeId, Set<AgentInfoBo>> agentMap = linkStatisticsData.getAgentMap();
        // 변경하면 안됨
        final List<Node> sourceNode = createSourceNode(linkStatisticsData, agentMap);
        nodeMap.addApplication(sourceNode);


        // indexing application (UI의 서버맵을 그릴 때 key 정보가 필요한데 unique해야하고 link정보와 맞춰야 됨.)
        nodeMap.indexingNode();
        // 변경하면 안됨.
        List<Link> sourceLink = createSourceLink(linkStatisticsData, nodeMap);
        nodeMap.addLink(sourceLink);


        nodeMap.buildApplication();

        return nodeMap;
    }

    private List<Link> createSourceLink(LinkStatisticsData rawData, ApplicationMap nodeMap) {
        final List<Link> result = new ArrayList<Link>();
        // extract relation
        for (LinkStatistics stat : rawData.getLinkStatData()) {
            final NodeId fromApplicationId = stat.getFromApplicationId();
            Node from = nodeMap.findApplication(fromApplicationId);
            // TODO
            final NodeId toApplicationId = stat.getToApplicationId();
            Node to = nodeMap.findApplication(toApplicationId);

            // rpc client가 빠진경우임.
            if (to == null) {
                continue;
            }

            // RPC client인 경우 dest application이 이미 있으면 삭제, 없으면 unknown cloud로 변경.
            HostList toHostList = stat.getToHostList();
            Link link = new Link(from, to, toHostList);
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

    private List<Node> createSourceNode(LinkStatisticsData rawData, Map<NodeId, Set<AgentInfoBo>> agentMap) {
        final List<Node> result = new ArrayList<Node>();
        // extract application and histogram
        for (LinkStatistics stat : rawData.getLinkStatData()) {
            // FROM -> TO에서 FROM이 CLIENT가 아니면 FROM은 application
            if (!stat.getFromServiceType().isRpcClient()) {
                final NodeId id = stat.getFromApplicationId();
                final Set<AgentInfoBo> agentSet = agentMap.get(id);
                // FIXME from은 tohostlist를 보관하지 않아서 없음. null로 입력. 그렇지 않으면 이상해짐 ㅡㅡ;
                Node node = new Node(id, stat.getFromApplication(), agentSet);
                result.add(node);
            }

            // FROM -> TO에서 TO가 CLIENT가 아니면 TO는 application
            if (!stat.getToServiceType().isRpcClient()) {
                final NodeId to = stat.getToApplicationId();

                Node node = new Node(to, stat.getToApplication(), stat.getToHostList());
                result.add(node);
            }
        }
        return result;
    }

}
