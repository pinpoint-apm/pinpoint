package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkData;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkDataMap;
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

    public ApplicationMap build(LinkDataDuplexMap linkStatisticsData, AgentSelector agentSelector) {
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

        LinkDataMap targetLinkData = linkStatisticsData.getTargetLinkData();
        logger.debug("----------------targetLinkData:{}", targetLinkData.size());
        for (LinkData statistics : targetLinkData.getLinkStatData()) {
            logger.debug("target:{}", statistics);
        }
        nodeMap.appendAgentInfo(linkStatisticsData, agentSelector);


        // 변경하면 안됨.
        List<Link> sourceLink = createSourceLink(linkStatisticsData.getSourceLinkData(), nodeMap);
        logger.debug("sourceLink.size:{}", sourceLink.size());
        nodeMap.addLink(sourceLink);


        List<Link> targetLink = createTargetLink(linkStatisticsData.getTargetLinkData(), nodeMap);
        logger.debug("targetLink.size:{}", targetLink.size());
        nodeMap.addLink(targetLink);


        return nodeMap;
    }

    private List<Link> createSourceLink(LinkDataMap rawData, ApplicationMap nodeMap) {
        final List<Link> result = new ArrayList<Link>();
        // extract relation
        for (LinkData linkStat : rawData.getLinkStatData()) {
            final Application fromApplicationId = linkStat.getFromApplication();
            Node fromNode = nodeMap.findNode(fromApplicationId);
            // TODO
            final Application toApplicationId = linkStat.getToApplication();
            Node toNode = nodeMap.findNode(toApplicationId);

            // rpc client가 빠진경우임.
            if (toNode == null) {
                continue;
            }

            // RPC client인 경우 dest application이 이미 있으면 삭제, 없으면 unknown cloud로 변경.
            LinkCallDataMap callDataMap = linkStat.getLinkCallDataMap();
            final Link link = new Link(fromNode, toNode, range, callDataMap, new LinkCallDataMap());

            if (toNode.getServiceType().isRpcClient()) {
                if (!nodeMap.containsNode(toNode.getApplication().getName())) {
                    result.add(link);
                }
            } else {
                result.add(link);
            }
        }
        return result;
    }



    private List<Link> createTargetLink(LinkDataMap rawData, ApplicationMap nodeMap) {
        final List<Link> result = new ArrayList<Link>();
        // extract relation
        for (LinkData linkStat : rawData.getLinkStatData()) {
            final Application fromApplicationId = linkStat.getFromApplication();
            Node fromNode = nodeMap.findNode(fromApplicationId);
            // TODO
            final Application toApplicationId = linkStat.getToApplication();
            Node toNode = nodeMap.findNode(toApplicationId);

            // rpc client가 빠진경우임.
            if (fromNode == null) {
                continue;
            }

            // RPC client인 경우 dest application이 이미 있으면 삭제, 없으면 unknown cloud로 변경.
            LinkCallDataMap callDataMap = linkStat.getLinkCallDataMap();

            Link link;
            if (fromNode.getApplication().getServiceType().isUser()) {
                link = new Link(fromNode, toNode, range, callDataMap, new LinkCallDataMap());
            } else {
                link = new Link(fromNode, toNode, range, new LinkCallDataMap(), callDataMap);
            }

            if (toNode.getServiceType().isRpcClient()) {
                if (!nodeMap.containsNode(toNode.getApplication().getName())) {
                    result.add(link);
                }
            } else {
                result.add(link);
            }
        }
        return result;
    }

    private List<Node> createSourceNode(LinkDataDuplexMap linkStatData) {

        final List<Node> result = new ArrayList<Node>();
        // extract application and histogram
        for (LinkData linkStat : linkStatData.getSourceLinkStatData()) {
            final Application fromApplication = linkStat.getFromApplication();
            // FROM -> TO에서 FROM이 CLIENT가 아니면 FROM은 application
            if (!fromApplication.getServiceType().isRpcClient()) {
                Node fromNode = new Node(fromApplication);
                result.add(fromNode);
            }


            final Application toApplication = linkStat.getToApplication();
            // FROM -> TO에서 TO가 CLIENT가 아니면 TO는 application
            if (!toApplication.getServiceType().isRpcClient()) {
                Node toNode = new Node(toApplication);
                result.add(toNode);
            }
        }
        if (logger.isDebugEnabled()) {
            Collection<LinkData> targetLinkStatData = linkStatData.getSourceLinkStatData();
            for (LinkData linkData : targetLinkStatData) {
                logger.debug("---------------target:{}", linkData);
            }
        }

        return result;
    }

    private List<Node> createTargetNode(LinkDataDuplexMap linkStatData) {

        final List<Node> result = new ArrayList<Node>();
        // extract application and histogram
        for (LinkData linkStat : linkStatData.getTargetLinkStatData()) {
            final Application fromApplication = linkStat.getFromApplication();
            // FROM -> TO에서 FROM이 CLIENT가 아니면 FROM은 application
            if (!fromApplication.getServiceType().isRpcClient()) {
                // FIXME from은 tohostlist를 보관하지 않아서 없음. null로 입력. 그렇지 않으면 이상해짐 ㅡㅡ;
                Node fromNode = new Node(fromApplication);
                result.add(fromNode);
            }


            final Application toApplication = linkStat.getToApplication();
            // FROM -> TO에서 TO가 CLIENT가 아니면 TO는 application
            if (!toApplication.getServiceType().isRpcClient()) {
                Node toNode = new Node(toApplication);
                result.add(toNode);
            }
        }
        if (logger.isDebugEnabled()) {
            Collection<LinkData> targetLinkStatData = linkStatData.getSourceLinkStatData();
            for (LinkData linkData : targetLinkStatData) {
                logger.debug("---------------target:{}", linkData);
            }
        }

        return result;
    }

}
