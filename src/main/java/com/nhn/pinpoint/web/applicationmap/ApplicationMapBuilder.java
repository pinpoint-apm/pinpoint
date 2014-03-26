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

    public ApplicationMap build(LinkDataDuplexMap linkDataDuplexMap, AgentSelector agentSelector) {
        if (linkDataDuplexMap == null) {
            throw new NullPointerException("linkDataMap must not be null");
        }

        final ApplicationMap map = new ApplicationMap(range);
        buildNode(map, linkDataDuplexMap, agentSelector);

        buildLink(map, linkDataDuplexMap);


        return map;
    }

    private void buildNode(ApplicationMap map, LinkDataDuplexMap linkDataDuplexMap, AgentSelector agentSelector) {
        final List<Node> sourceNode = createNode(linkDataDuplexMap.getSourceLinkData());
        map.addNodeList(sourceNode);
        logger.debug("sourceNode:{}", sourceNode);

        final List<Node> targetNode = createNode(linkDataDuplexMap.getTargetLinkData());
        map.addNodeList(targetNode);
        logger.debug("targetNode:{}", targetNode);

        // agentInfo를 넣는다.
        map.appendAgentInfo(linkDataDuplexMap, agentSelector);
        logger.debug("allNode:{}", map.getNodes());
    }

    private List<Node> createNode(LinkDataMap linkDataMap) {

        final List<Node> result = new ArrayList<Node>();

        for (LinkData linkData : linkDataMap.getLinkDataList()) {
            final Application fromApplication = linkData.getFromApplication();
            // FROM -> TO에서 FROM이 CLIENT가 아니면 FROM은 node
            if (!fromApplication.getServiceType().isRpcClient()) {
                Node fromNode = new Node(fromApplication);
                result.add(fromNode);
            } else {
                logger.warn("found rpc fromNode linkData:{}", linkData);
            }


            final Application toApplication = linkData.getToApplication();
            // FROM -> TO에서 TO가 CLIENT가 아니면 TO는 node
            if (!toApplication.getServiceType().isRpcClient()) {
                Node toNode = new Node(toApplication);
                result.add(toNode);
            } else {
                logger.warn("found rpc toNode:{}", linkData);
            }
        }
        return result;
    }

    private void buildLink(ApplicationMap map, LinkDataDuplexMap linkDataDuplexMap) {
        // 변경하면 안됨.
        List<Link> sourceLink = createSourceLink(linkDataDuplexMap.getSourceLinkData(), map);
        logger.debug("sourceLink.size:{}", sourceLink.size());
        map.addLink(sourceLink);


        List<Link> targetLink = createTargetLink(linkDataDuplexMap.getTargetLinkData(), map);
        logger.debug("targetLink.size:{}", targetLink.size());
        map.addLink(targetLink);

        LinkDataMap targetLinkData = linkDataDuplexMap.getTargetLinkData();
        logger.debug("----------------targetLinkData:{}", targetLinkData.size());
        for (LinkData statistics : targetLinkData.getLinkDataList()) {
            logger.debug("target:{}", statistics);
        }
    }

    private List<Link> createSourceLink(LinkDataMap linkDataMap, ApplicationMap map) {
        final List<Link> result = new ArrayList<Link>();

        for (LinkData linkData : linkDataMap.getLinkDataList()) {
            final Application fromApplicationId = linkData.getFromApplication();
            Node fromNode = map.findNode(fromApplicationId);
            // TODO
            final Application toApplicationId = linkData.getToApplication();
            Node toNode = map.findNode(toApplicationId);

            // rpc client가 빠진경우임.
            if (toNode == null) {
                logger.warn("rcp client not found:{}", toApplicationId);
                continue;
            }

            // RPC client인 경우 dest application이 이미 있으면 삭제, 없으면 unknown cloud로 변경.
            LinkCallDataMap callDataMap = linkData.getLinkCallDataMap();
            final Link link = new Link(fromNode, toNode, range, callDataMap, new LinkCallDataMap());

            if (toNode.getServiceType().isRpcClient()) {
                if (!map.containsNode(toNode.getApplication().getName())) {
                    result.add(link);
                }
            } else {

                result.add(link);
            }
        }
        return result;
    }



    private List<Link> createTargetLink(LinkDataMap rawData, ApplicationMap map) {
        final List<Link> result = new ArrayList<Link>();
        // extract relation
        for (LinkData linkData : rawData.getLinkDataList()) {
            final Application fromApplicationId = linkData.getFromApplication();
            Node fromNode = map.findNode(fromApplicationId);
            // TODO
            final Application toApplicationId = linkData.getToApplication();
            Node toNode = map.findNode(toApplicationId);

            // rpc client가 빠진경우임.
            if (fromNode == null) {
                continue;
            }

            // RPC client인 경우 dest application이 이미 있으면 삭제, 없으면 unknown cloud로 변경.
            LinkCallDataMap callDataMap = linkData.getLinkCallDataMap();

            Link link;
            if (fromNode.getApplication().getServiceType().isUser()) {
                link = new Link(fromNode, toNode, range, callDataMap, new LinkCallDataMap());
            } else {
                link = new Link(fromNode, toNode, range, new LinkCallDataMap(), callDataMap);
            }

            if (toNode.getServiceType().isRpcClient()) {
                if (!map.containsNode(toNode.getApplication().getName())) {
                    result.add(link);
                }
            } else {
                result.add(link);
            }
        }
        return result;
    }



}
