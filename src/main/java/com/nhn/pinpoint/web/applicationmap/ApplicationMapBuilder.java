package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatisticsData;
import com.nhn.pinpoint.web.applicationmap.rawdata.RawCallDataMap;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class ApplicationMapBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Range range;

    public ApplicationMapBuilder(Range range) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }

        this.range = range;
    }

    public ApplicationMap build(LinkStatisticsData linkStatisticsData) {
        if (linkStatisticsData == null) {
            throw new NullPointerException("linkStatData must not be null");
        }

        final ApplicationMap nodeMap = new ApplicationMap(range);

        // extract agent
        Map<Application, Set<AgentInfoBo>> agentMap = linkStatisticsData.getAgentMap();
        // 변경하면 안됨
        final List<Node> sourceNode = createSourceNode(linkStatisticsData, agentMap);
        nodeMap.addNode(sourceNode);


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
            RawCallDataMap callDataMap = new RawCallDataMap(linkStat.getCallDataMap());
            final Link link = new Link(fromNode, toNode, range, callDataMap);

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
                Node toNode = new Node(toApplication, linkStat.getTargetList());
                result.add(toNode);
            }
        }
        return result;
    }

}
