package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatisticsData;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatisticsDataSet;
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

    public ApplicationMap build(LinkStatisticsDataSet linkStatisticsData) {
        if (linkStatisticsData == null) {
            throw new NullPointerException("linkStatData must not be null");
        }

        final ApplicationMap nodeMap = new ApplicationMap(range);

        // extract agent
//        Map<Application, Set<AgentInfoBo>> agentMap = linkStatisticsData.getSourceAgentMap();
        // 변경하면 안됨
        final List<Node> sourceNode = createSourceNode(linkStatisticsData);
        nodeMap.addNode(sourceNode);

        List<Node> targetNode = createTargetNode(linkStatisticsData);
        nodeMap.addNode(targetNode);
        logger.debug("targetNode:{}", targetNode);

        LinkStatisticsData targetLinkData = linkStatisticsData.getTargetLinkData();
        logger.debug("----------------targetLinkData:{}", targetLinkData.size());
        for (LinkStatistics statistics : targetLinkData.getLinkStatData()) {
            logger.debug("target:{}", statistics);
        }



        // 변경하면 안됨.
        List<Link> sourceLink = createSourceLink(linkStatisticsData.getSourceLinkData(), nodeMap);
        logger.debug("sourceLink.size:{}", sourceLink.size());
        nodeMap.addLink(sourceLink);


        List<Link> targetLink = createTargetLink(linkStatisticsData.getTargetLinkData(), nodeMap);
        logger.debug("targetLink.size:{}", targetLink.size());
        nodeMap.addLink(targetLink);


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
            RawCallDataMap callDataMap = linkStat.getCallDataMap();
            final Link link = new Link(fromNode, toNode, range, callDataMap, new RawCallDataMap());

            if (toNode.getServiceType().isRpcClient()) {
                if (!nodeMap.containsApplicationName(toNode.getApplication().getName())) {
                    result.add(link);
                }
            } else {
                result.add(link);
            }
        }
        return result;
    }



    private List<Link> createTargetLink(LinkStatisticsData rawData, ApplicationMap nodeMap) {
        final List<Link> result = new ArrayList<Link>();
        // extract relation
        for (LinkStatistics linkStat : rawData.getLinkStatData()) {
            final Application fromApplicationId = linkStat.getFromApplication();
            Node fromNode = nodeMap.findApplication(fromApplicationId);
            // TODO
            final Application toApplicationId = linkStat.getToApplication();
            Node toNode = nodeMap.findApplication(toApplicationId);

            // rpc client가 빠진경우임.
            if (fromNode == null) {
                continue;
            }

            // RPC client인 경우 dest application이 이미 있으면 삭제, 없으면 unknown cloud로 변경.
            RawCallDataMap callDataMap = linkStat.getCallDataMap();

            Link link;
            if (fromNode.getApplication().getServiceType().isUser()) {
                link = new Link(fromNode, toNode, range, callDataMap, new RawCallDataMap());
            } else {
                link = new Link(fromNode, toNode, range, new RawCallDataMap(), callDataMap);
            }

            if (toNode.getServiceType().isRpcClient()) {
                if (!nodeMap.containsApplicationName(toNode.getApplication().getName())) {
                    result.add(link);
                }
            } else {
                result.add(link);
            }
        }
        return result;
    }

    private List<Node> createSourceNode(LinkStatisticsDataSet linkStatData) {

        Map<Application, Set<AgentInfoBo>> targetAgentMap = linkStatData.getTargetLinkData().getAgentMap();

        final List<Node> result = new ArrayList<Node>();
        // extract application and histogram
        for (LinkStatistics linkStat : linkStatData.getSourceLinkStatData()) {
            final Application fromApplication = linkStat.getFromApplication();
            // FROM -> TO에서 FROM이 CLIENT가 아니면 FROM은 application
            if (!fromApplication.getServiceType().isRpcClient()) {
                final Set<AgentInfoBo> agentSet = targetAgentMap.get(fromApplication);
                // FIXME from은 tohostlist를 보관하지 않아서 없음. null로 입력. 그렇지 않으면 이상해짐 ㅡㅡ;
                Node fromNode = new Node(fromApplication, agentSet);
                result.add(fromNode);
            }


            final Application toApplication = linkStat.getToApplication();
            // FROM -> TO에서 TO가 CLIENT가 아니면 TO는 application
            if (!toApplication.getServiceType().isRpcClient()) {
                Node toNode = new Node(toApplication, linkStat.getTargetList());
                result.add(toNode);
            }
        }
        if (logger.isDebugEnabled()) {
            Collection<LinkStatistics> targetLinkStatData = linkStatData.getSourceLinkStatData();
            for (LinkStatistics linkStatistics : targetLinkStatData) {
                logger.debug("---------------target:{}", linkStatistics);
            }
        }

        return result;
    }

    private List<Node> createTargetNode(LinkStatisticsDataSet linkStatData) {

        Map<Application, Set<AgentInfoBo>> sourceAgentMap = linkStatData.getSourceLinkData().getAgentMap();

        final List<Node> result = new ArrayList<Node>();
        // extract application and histogram
        for (LinkStatistics linkStat : linkStatData.getTargetLinkStatData()) {
            final Application fromApplication = linkStat.getFromApplication();
            // FROM -> TO에서 FROM이 CLIENT가 아니면 FROM은 application
            if (!fromApplication.getServiceType().isRpcClient()) {
                // FIXME from은 tohostlist를 보관하지 않아서 없음. null로 입력. 그렇지 않으면 이상해짐 ㅡㅡ;
                Node fromNode = new Node(fromApplication, linkStat.getTargetList());
                result.add(fromNode);
            }


            final Application toApplication = linkStat.getToApplication();
            // FROM -> TO에서 TO가 CLIENT가 아니면 TO는 application
            if (!toApplication.getServiceType().isRpcClient()) {
                Node toNode = new Node(toApplication, linkStat.getTargetList());
                result.add(toNode);
            }
        }
        if (logger.isDebugEnabled()) {
            Collection<LinkStatistics> targetLinkStatData = linkStatData.getSourceLinkStatData();
            for (LinkStatistics linkStatistics : targetLinkStatData) {
                logger.debug("---------------target:{}", linkStatistics);
            }
        }

        return result;
    }

}
