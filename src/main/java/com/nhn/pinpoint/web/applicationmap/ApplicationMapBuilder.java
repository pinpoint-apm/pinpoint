package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.rawdata.HostList;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatisticsData;
import com.nhn.pinpoint.web.vo.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class ApplicationMapBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ApplicationMapBuilder() {
    }

    public ApplicationMap build(List<LinkStatistics> linkStatistics) {
        if (linkStatistics == null) {
            throw new NullPointerException("linkStatData must not be null");
        }
        final LinkStatisticsData linkStatisticsData = new LinkStatisticsData(linkStatistics);

        final ApplicationMap nodeMap = new ApplicationMap();

        // extract agent
        Map<Application, Set<AgentInfoBo>> agentMap = linkStatisticsData.getAgentMap();
        // 변경하면 안됨
        final List<Node> sourceNode = createSourceNode(linkStatisticsData, agentMap);
        nodeMap.addNode(sourceNode);


        // indexing application (UI의 서버맵을 그릴 때 key 정보가 필요한데 unique해야하고 link정보와 맞춰야 됨.)
        nodeMap.indexingNode();
        // 변경하면 안됨.
        List<Link> sourceLink = createSourceLink(linkStatisticsData, nodeMap);
        nodeMap.addLink(sourceLink);


        nodeMap.buildNode();

        return nodeMap;
    }

    private List<Link> createSourceLink(LinkStatisticsData rawData, ApplicationMap nodeMap) {
        final List<Link> result = new ArrayList<Link>();
        // extract relation
        for (LinkStatistics linkStat : rawData.getLinkStatData()) {
            final Application fromApplicationId = linkStat.getFromApplication();
            Node fromNode = nodeMap.findApplication(fromApplicationId);
            // TODO
            final Application toApplicationId = linkStat.getToApplication();
            Node toNode = nodeMap.findApplication(toApplicationId);

            // rpc client가 빠진경우임.
            if (toNode == null) {
                continue;
            }

            // RPC client인 경우 dest application이 이미 있으면 삭제, 없으면 unknown cloud로 변경.
            HostList toHostList = linkStat.getToHostList();
            Link link = new Link(fromNode, toNode, toHostList);
            link.setSourceList(linkStat.getSourceList());
            if (toNode.getServiceType().isRpcClient()) {
                if (!nodeMap.containsApplicationName(toNode.getApplicationName())) {
                    result.add(link);
                }
            } else {
                result.add(link);
            }
        }
        return result;
    }

    private List<Node> createSourceNode(LinkStatisticsData linkStatData, Map<Application, Set<AgentInfoBo>> agentMap) {
        final List<Node> result = new ArrayList<Node>();
        // extract application and histogram
        for (LinkStatistics linkStat : linkStatData.getLinkStatData()) {
            // FROM -> TO에서 FROM이 CLIENT가 아니면 FROM은 application
            if (!linkStat.getFromServiceType().isRpcClient()) {
                final Application fromApplication = linkStat.getFromApplication();
                final Set<AgentInfoBo> agentSet = agentMap.get(fromApplication);
                // FIXME from은 tohostlist를 보관하지 않아서 없음. null로 입력. 그렇지 않으면 이상해짐 ㅡㅡ;
                Node fromNode = new Node(fromApplication, agentSet);
                result.add(fromNode);
            }

            // FROM -> TO에서 TO가 CLIENT가 아니면 TO는 application
            if (!linkStat.getToServiceType().isRpcClient()) {
                final Application toApplication = linkStat.getToApplication();
                Node toNode = new Node(toApplication, linkStat.getToHostList());
                result.add(toNode);
            }
        }
        return result;
    }

}
