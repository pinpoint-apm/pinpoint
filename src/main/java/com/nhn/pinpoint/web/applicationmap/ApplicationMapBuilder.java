package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.web.applicationmap.rawdata.LinkData;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.nhn.pinpoint.web.service.AgentInfoService;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.LinkKey;
import com.nhn.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public ApplicationMap build(LinkDataDuplexMap linkDataDuplexMap, AgentInfoService agentInfoService) {
        if (linkDataDuplexMap == null) {
            throw new NullPointerException("linkDataMap must not be null");
        }
        if (agentInfoService == null) {
            throw new NullPointerException("agentInfoService must not be null");
        }


        final ApplicationMap map = new ApplicationMap(range);
        buildNode(map, linkDataDuplexMap, agentInfoService);

        buildLink(map, linkDataDuplexMap);


        return map;
    }

    private void buildNode(ApplicationMap map, LinkDataDuplexMap linkDataDuplexMap, AgentInfoService agentInfoService) {
        NodeList nodeList = new NodeList();
        createNode(nodeList, linkDataDuplexMap.getSourceLinkDataMap());
        logger.debug("node size:{}", nodeList.size());
        createNode(nodeList, linkDataDuplexMap.getTargetLinkDataMap());
        logger.debug("node size:{}", nodeList.size());
        map.addNodeList(nodeList);


        // agentInfo를 넣는다.
        map.appendAgentInfo(linkDataDuplexMap, agentInfoService);
        logger.debug("allNode:{}", map.getNodes());
    }

    private void createNode(NodeList nodeList, LinkDataMap linkDataMap) {

        for (LinkData linkData : linkDataMap.getLinkDataList()) {
            final Application fromApplication = linkData.getFromApplication();
            // FROM -> TO에서 FROM이 CLIENT가 아니면 FROM은 node
            // rpc가 나올수가 없음. 이미 unknown으로 치환을 하기 때문에. 만약 rpc가 나온다면 이상한 케이스임
            if (!fromApplication.getServiceType().isRpcClient()) {
                final boolean success = addNode(nodeList, fromApplication);
                if (success) {
                    logger.debug("createSourceNode:{}", fromApplication);
                }
            } else {
                logger.warn("found rpc fromNode linkData:{}", linkData);
            }


            final Application toApplication = linkData.getToApplication();
            // FROM -> TO에서 TO가 CLIENT가 아니면 TO는 node
            if (!toApplication.getServiceType().isRpcClient()) {
                final boolean success = addNode(nodeList, toApplication);
                if (success) {
                    logger.debug("createTargetNode:{}", toApplication);
                }
            } else {
                logger.warn("found rpc toNode:{}", linkData);
            }
        }

    }

    private boolean addNode(NodeList nodeList, Application application) {
        if (nodeList.containsNode(application)) {
            return false;
        }

        Node fromNode = new Node(application);
        return nodeList.addNode(fromNode);
    }

    private void buildLink(ApplicationMap map, LinkDataDuplexMap linkDataDuplexMap) {
        // 변경하면 안됨.
        LinkList linkList = new LinkList();
        createSourceLink(linkList, linkDataDuplexMap.getSourceLinkDataMap(), map);
        logger.debug("link size:{}", linkList.size());
        createTargetLink(linkList, linkDataDuplexMap.getTargetLinkDataMap(), map);
        logger.debug("link size:{}", linkList.size());
        map.addLinkList(linkList);


        for (Link link : map.getLinks()) {
            appendLinkHistogram(link, linkDataDuplexMap);
        }

    }

    private void appendLinkHistogram(Link link, LinkDataDuplexMap linkDataDuplexMap) {
        logger.debug("appendLinkHistogram link:{}", link);

        LinkKey key = link.getLinkKey();
        LinkData sourceLinkData = linkDataDuplexMap.getSourceLinkData(key);
        if (sourceLinkData != null) {
            link.addSource(sourceLinkData.getLinkCallDataMap());
        }
        LinkData targetLinkData = linkDataDuplexMap.getTargetLinkData(key);
        if (targetLinkData != null) {
            link.addTarget(targetLinkData.getLinkCallDataMap());
        }
    }

    private void createSourceLink(LinkList linkList, LinkDataMap linkDataMap, ApplicationMap map) {

        for (LinkData linkData : linkDataMap.getLinkDataList()) {
            final Application fromApplicationId = linkData.getFromApplication();
            Node fromNode = map.findNode(fromApplicationId);

            final Application toApplicationId = linkData.getToApplication();
            Node toNode = map.findNode(toApplicationId);

            // rpc client가 빠진경우임.
            if (toNode == null) {
                logger.warn("toNode rcp client not found:{}", toApplicationId);
                continue;
            }

            // RPC client인 경우 dest application이 이미 있으면 삭제, 없으면 unknown cloud로 변경
            // 여기서 RPC가 나올일이 없지 않나하는데. 먼저 앞단에서 Unknown노드로 변경시킴.
            if (toNode.getServiceType().isRpcClient()) {
                if (!map.containsNode(toNode.getApplication())) {
                    final Link link = addLink(linkList, fromNode, toNode, CreateType.Source);
                    if (link != null) {
                        logger.debug("createRpcSourceLink:{}", link);
                    }
                }
            } else {
                final Link link = addLink(linkList, fromNode, toNode, CreateType.Source);
                if (link != null) {
                    logger.debug("createSourceLink:{}", link);
                }
            }
        }
    }

    private Link addLink(LinkList linkList, Node fromNode, Node toNode, CreateType createType) {
        final Link link = new Link(createType, fromNode, toNode, range);
        if (linkList.addLink(link)) {
            return link;
        } else {
            return null;
        }

    }


    private void createTargetLink(LinkList linkList, LinkDataMap rawData, ApplicationMap map) {

        for (LinkData linkData : rawData.getLinkDataList()) {
            final Application fromApplicationId = linkData.getFromApplication();
            Node fromNode = map.findNode(fromApplicationId);
            // TODO
            final Application toApplicationId = linkData.getToApplication();
            Node toNode = map.findNode(toApplicationId);

            // rpc client가 빠진경우임.
            if (fromNode == null) {
                logger.warn("fromNode rcp client not found:{}", toApplicationId);
                continue;
            }

            // RPC client인 경우 dest application이 이미 있으면 삭제, 없으면 unknown cloud로 변경.
            if (toNode.getServiceType().isRpcClient()) {
                // to 노드가 존재하는지 검사?
                if (!map.containsNode(toNode.getApplication())) {
                    final Link link = addLink(linkList, fromNode, toNode, CreateType.Target);
                    if(link != null) {
                        logger.debug("createRpcTargetLink:{}", link);
                    }
                }
            } else {
                final Link link = addLink(linkList, fromNode, toNode, CreateType.Target);
                if(link != null) {
                    logger.debug("createTargetLink:{}", link);
                }
            }
        }
    }






}
