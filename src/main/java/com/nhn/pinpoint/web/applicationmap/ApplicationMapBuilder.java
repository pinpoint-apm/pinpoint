package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.histogram.*;
import com.nhn.pinpoint.web.applicationmap.rawdata.*;
import com.nhn.pinpoint.web.dao.MapResponseDao;
import com.nhn.pinpoint.web.service.AgentInfoService;
import com.nhn.pinpoint.web.vo.*;
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

    public ApplicationMap build(LinkDataDuplexMap linkDataDuplexMap, AgentInfoService agentInfoService, NodeHistogramDataSource nodeHistogramDataSource) {
        if (linkDataDuplexMap == null) {
            throw new NullPointerException("linkDataMap must not be null");
        }
        if (agentInfoService == null) {
            throw new NullPointerException("agentInfoService must not be null");
        }

        NodeList nodeList = buildNode(linkDataDuplexMap);
        LinkList linkList = buildLink(nodeList, linkDataDuplexMap);


        appendNodeResponseTime(nodeList, linkList, nodeHistogramDataSource);
        // agentInfo를 넣는다.
        appendAgentInfo(nodeList, linkDataDuplexMap, agentInfoService);

        final ApplicationMap map = new ApplicationMap(range, nodeList, linkList);
        return map;
    }


    public ApplicationMap build(LinkDataDuplexMap linkDataDuplexMap, AgentInfoService agentInfoService, final MapResponseDao mapResponseDao) {
        NodeHistogramDataSource responseSource = new NodeHistogramDataSource() {
            @Override
            public NodeHistogram createNodeHistogram(Application application) {
                final List<ResponseTime> responseHistogram = mapResponseDao.selectResponseTime(application, range);
                final NodeHistogram nodeHistogram = new NodeHistogram(application, range, responseHistogram);
                return nodeHistogram;
            }
        };
        return this.build(linkDataDuplexMap, agentInfoService, responseSource);
    }

    public ApplicationMap build(LinkDataDuplexMap linkDataDuplexMap, AgentInfoService agentInfoService, final ResponseHistogramBuilder mapHistogramSummary) {
        NodeHistogramDataSource responseSource = new NodeHistogramDataSource() {
            @Override
            public NodeHistogram createNodeHistogram(Application application) {
                List<ResponseTime> responseHistogram = mapHistogramSummary.getResponseTimeList(application);
                final NodeHistogram nodeHistogram = new NodeHistogram(application, range, responseHistogram);
                return nodeHistogram;
            }
        };
        return this.build(linkDataDuplexMap, agentInfoService, responseSource);
    }

    public interface NodeHistogramDataSource {
        NodeHistogram createNodeHistogram(Application application);
    }


    private NodeList buildNode(LinkDataDuplexMap linkDataDuplexMap) {
        NodeList nodeList = new NodeList();
        createNode(nodeList, linkDataDuplexMap.getSourceLinkDataMap());
        logger.debug("node size:{}", nodeList.size());
        createNode(nodeList, linkDataDuplexMap.getTargetLinkDataMap());
        logger.debug("node size:{}", nodeList.size());

        logger.debug("allNode:{}", nodeList.getNodeList());
        return nodeList;
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

    private LinkList buildLink(NodeList nodeList, LinkDataDuplexMap linkDataDuplexMap) {
        // 변경하면 안됨.
        LinkList linkList = new LinkList();
        createSourceLink(nodeList, linkList, linkDataDuplexMap.getSourceLinkDataMap());
        logger.debug("link size:{}", linkList.size());
        createTargetLink(nodeList, linkList, linkDataDuplexMap.getTargetLinkDataMap());
        logger.debug("link size:{}", linkList.size());

        for (Link link : linkList.getLinkList()) {
            appendLinkHistogram(link, linkDataDuplexMap);
        }
        return linkList;
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

    private void createSourceLink(NodeList nodeList, LinkList linkList, LinkDataMap linkDataMap) {

        for (LinkData linkData : linkDataMap.getLinkDataList()) {
            final Application fromApplicationId = linkData.getFromApplication();
            Node fromNode = nodeList.findNode(fromApplicationId);

            final Application toApplicationId = linkData.getToApplication();
            Node toNode = nodeList.findNode(toApplicationId);

            // rpc client가 빠진경우임.
            if (toNode == null) {
                logger.warn("toNode rcp client not found:{}", toApplicationId);
                continue;
            }

            // RPC client인 경우 dest application이 이미 있으면 삭제, 없으면 unknown cloud로 변경
            // 여기서 RPC가 나올일이 없지 않나하는데. 먼저 앞단에서 Unknown노드로 변경시킴.
            if (toNode.getServiceType().isRpcClient()) {
                if (!nodeList.containsNode(toNode.getApplication())) {
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


    private void createTargetLink(NodeList nodeList, LinkList linkList, LinkDataMap linkDataMap) {

        for (LinkData linkData : linkDataMap.getLinkDataList()) {
            final Application fromApplicationId = linkData.getFromApplication();
            Node fromNode = nodeList.findNode(fromApplicationId);
            // TODO
            final Application toApplicationId = linkData.getToApplication();
            Node toNode = nodeList.findNode(toApplicationId);

            // rpc client가 빠진경우임.
            if (fromNode == null) {
                logger.warn("fromNode rcp client not found:{}", toApplicationId);
                continue;
            }

            // RPC client인 경우 dest application이 이미 있으면 삭제, 없으면 unknown cloud로 변경.
            if (toNode.getServiceType().isRpcClient()) {
                // to 노드가 존재하는지 검사?
                if (!nodeList.containsNode(toNode.getApplication())) {
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

    public void appendNodeResponseTime(NodeList nodeList, LinkList linkList, NodeHistogramDataSource nodeHistogramDataSource) {
        if (nodeHistogramDataSource == null) {
            throw new NullPointerException("nodeHistogramDataSource must not be null");
        }

        final Collection<Node> nodes = nodeList.getNodeList();
        for (Node node : nodes) {
            final ServiceType nodeType = node.getServiceType();
            if (nodeType.isWas()) {
                // was일 경우 자신의 response 히스토그램을 조회하여 채운다.
                final Application wasNode = node.getApplication();
                final NodeHistogram nodeHistogram = nodeHistogramDataSource.createNodeHistogram(wasNode);
                node.setNodeHistogram(nodeHistogram);

            } else if(nodeType.isTerminal() || nodeType.isUnknown()) {
                final NodeHistogram nodeHistogram = createTerminalNodeHistogram(node, linkList);
                node.setNodeHistogram(nodeHistogram);
            } else if (nodeType.isUser()) {
                // User노드인 경우 source 링크를 찾아 histogram을 생성한다.
                Application userNode = node.getApplication();

                final NodeHistogram nodeHistogram = new NodeHistogram(userNode, range);
                final List<Link> fromLink = linkList.findFromLink(userNode);
                if (fromLink.size() > 1) {
                    logger.warn("Invalid from UserNode:{}", linkList);
                    throw new IllegalArgumentException("Invalid from UserNode.size() :" + fromLink.size());
                } else if (fromLink.size() == 0) {
                    logger.warn("from UserNode not found:{}", userNode);
                    continue;
                }
                final Link sourceLink = fromLink.get(0);
                nodeHistogram.setApplicationHistogram(sourceLink.getHistogram());

                ApplicationTimeHistogram histogramData = sourceLink.getTargetApplicationTimeSeriesHistogramData();
                nodeHistogram.setApplicationTimeHistogram(histogramData);

                node.setNodeHistogram(nodeHistogram);
            } else {
                // 그냥 데미 데이터
                NodeHistogram dummy = new NodeHistogram(node.getApplication(), range);
                node.setNodeHistogram(dummy);
            }

        }

    }

    private NodeHistogram createTerminalNodeHistogram(Node node, LinkList linkList) {
        // 터미널 노드인경우, 자신을 가리키는 link값을 합하여 histogram을 생성한다.
        final Application nodeApplication = node.getApplication();
        final NodeHistogram nodeHistogram = new NodeHistogram(nodeApplication, range);

        // appclicationHistogram 생성.
        final List<Link> toLinkList = linkList.findToLink(nodeApplication);
        final Histogram applicationHistogram = new Histogram(node.getServiceType());
        for (Link link : toLinkList) {
            applicationHistogram.add(link.getHistogram());
        }
        nodeHistogram.setApplicationHistogram(applicationHistogram);

        // applicationTimeHistogram 생성.
        LinkCallDataMap linkCallDataMap = new LinkCallDataMap();
        for (Link link : toLinkList) {
            LinkCallDataMap sourceLinkCallDataMap = link.getSourceLinkCallDataMap();
            linkCallDataMap.addLinkDataMap(sourceLinkCallDataMap);
        }
        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(nodeApplication, range);
        ApplicationTimeHistogram applicationTimeHistogram = builder.build(linkCallDataMap.getLinkDataMap());
        nodeHistogram.setApplicationTimeHistogram(applicationTimeHistogram);

        // terminal일 경우 node의 AgentLevel histogram을 추가로 생성한다.
        if (nodeApplication.getServiceType().isTerminal()) {
            final Map<String, Histogram> agentHistogramMap = new HashMap<String, Histogram>();

            for (Link link : toLinkList) {
                LinkCallDataMap sourceLinkCallDataMap = link.getSourceLinkCallDataMap();
                AgentHistogramList targetList = sourceLinkCallDataMap.getTargetList();
                for (AgentHistogram histogram : targetList.getAgentHistogramList()) {
                    Histogram find = agentHistogramMap.get(histogram.getId());
                    if (find == null) {
                        find = new Histogram(histogram.getServiceType());
                        agentHistogramMap.put(histogram.getId(), find);
                    }
                    find.add(histogram.getHistogram());
                }
                nodeHistogram.setAgentHistogramMap(agentHistogramMap);
            }
        }

        LinkCallDataMap mergeSource = new LinkCallDataMap();
        for (Link link : toLinkList) {
            LinkCallDataMap sourceLinkCallDataMap = link.getSourceLinkCallDataMap();
            mergeSource.addLinkDataMap(sourceLinkCallDataMap);
        }

        AgentTimeHistogramBuilder agentTimeBuilder = new AgentTimeHistogramBuilder(nodeApplication, range);
        AgentTimeHistogram agentTimeHistogram = agentTimeBuilder.buildTarget(mergeSource);
        nodeHistogram.setAgentTimeHistogram(agentTimeHistogram);

        return nodeHistogram;
    }

    public void appendAgentInfo(NodeList nodeList, LinkDataDuplexMap linkDataDuplexMap, AgentInfoService agentInfoService) {
        for (Node node : nodeList.getNodeList()) {
            appendServerInfo(node, linkDataDuplexMap, agentInfoService);
        }

    }

    private void appendServerInfo(Node node, LinkDataDuplexMap linkDataDuplexMap, AgentInfoService agentInfoService) {
        final ServiceType nodeServiceType = node.getServiceType();
        if (nodeServiceType.isUnknown()) {
            // unknown노드는 무엇이 설치되어있는지 알수가 없음.
            return;
        }

        if (nodeServiceType.isTerminal()) {
            // terminal노드에 설치되어 있는 정보를 유추한다.
            ServerBuilder builder = new ServerBuilder();
            for (LinkData linkData : linkDataDuplexMap.getSourceLinkDataList()) {
                Application toApplication = linkData.getToApplication();
                if (node.getApplication().equals(toApplication)) {
                    builder.addCallHistogramList(linkData.getTargetList());
                }
            }
            ServerInstanceList serverInstanceList = builder.build();
            node.setServerInstanceList(serverInstanceList);
        } else if (nodeServiceType.isWas()) {
            Set<AgentInfoBo> agentList = agentInfoService.selectAgent(node.getApplication().getName(), range);
            if (agentList.isEmpty()) {
                return;
            }
            logger.debug("add agentInfo. {}, {}", node.getApplication(), agentList);
            ServerBuilder builder = new ServerBuilder();
            agentList = filterAgentInfoByResponseData(agentList, node);
            builder.addAgentInfo(agentList);
            ServerInstanceList serverInstanceList = builder.build();

            // destination이 WAS이고 agent가 설치되어있으면 agentSet이 존재한다.
            node.setServerInstanceList(serverInstanceList);
        }

    }

    /**
     * 실제 응답속도 정보가 있는 데이터를 기반으로 AgentInfo를 필터링 친다.
     * 정공이라고 말할 수 있는 코드는 아님.
     * 나중에 실제 서버가 살아 있는 정보를 기반으로 이를 유추할수 있게 해야한다.
     */
    private Set<AgentInfoBo> filterAgentInfoByResponseData(Set<AgentInfoBo> agentList, Node node) {
        Set<AgentInfoBo> filteredAgentInfo = new HashSet<AgentInfoBo>();

        NodeHistogram nodeHistogram = node.getNodeHistogram();
        Map<String, Histogram> agentHistogramMap = nodeHistogram.getAgentHistogramMap();
        for (AgentInfoBo agentInfoBo : agentList) {
            String agentId = agentInfoBo.getAgentId();
            if (agentHistogramMap.containsKey(agentId)) {
                filteredAgentInfo.add(agentInfoBo);
            }
        }

        return filteredAgentInfo;
    }


}
