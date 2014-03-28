package com.nhn.pinpoint.web.applicationmap;

import java.util.*;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkData;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.nhn.pinpoint.web.dao.MapResponseDao;
import com.nhn.pinpoint.web.service.AgentInfoService;
import com.nhn.pinpoint.web.vo.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node map
 * 
 * @author netspider
 * @author emeroad
 */
public class ApplicationMap {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final NodeList nodeList = new NodeList();
    private final LinkList linkList = new LinkList();

    private final Range range;


	ApplicationMap(Range range) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        this.range = range;
	}

    @JsonProperty("nodeDataArray")
    public Collection<Node> getNodes() {
		return this.nodeList.getNodeList();
	}

    @JsonProperty("linkDataArray")
	public Collection<Link> getLinks() {
		return this.linkList.getLinks();
	}


	Node findNode(Application applicationId) {
        return this.nodeList.findNode(applicationId);
	}

    void addNodeList(List<Node> nodeList) {
        this.nodeList.addNodeList(nodeList);
    }

    void addLink(List<Link> relationList) {
        linkList.buildLink(relationList);
    }



    public boolean containsNode(String applicationName) {
        return nodeList.containsNode(applicationName);
    }

    public void appendAgentInfo(LinkDataDuplexMap linkStatisticsData, AgentInfoService agentInfoService) {
        for (Node node : nodeList.getNodeList()) {
            appendServerInfo(node, linkStatisticsData, agentInfoService);
        }

    }

    private void appendServerInfo(Node node, LinkDataDuplexMap stat, AgentInfoService agentInfoService) {
        final ServiceType nodeServiceType = node.getServiceType();
        if (nodeServiceType.isUnknown()) {
            // unknown노드는 무엇이 설치되어있는지 알수가 없음.
            return;
        }

        if (nodeServiceType.isTerminal()) {
            // terminal노드에 설치되어 있는 정보를 유추한다.
            ServerBuilder builder = new ServerBuilder();
            Collection<LinkData> sourceLinkStatData = stat.getSourceLinkDataList();
            for (LinkData linkData : sourceLinkStatData) {
                Application toApplication = linkData.getToApplication();
                if (node.getApplication().equals(toApplication)) {
                    builder.addCallHistogramList(linkData.getTargetList());
                }
            }
            ServerInstanceList serverInstanceList = builder.build();
            node.setServerInstanceList(serverInstanceList);
        } else if (nodeServiceType.isWas()) {
            final Set<AgentInfoBo> agentList = agentInfoService.selectAgent(node.getApplication().getName());
            if (agentList.isEmpty()) {
                return;
            }
            logger.debug("add agentInfo. {}, {}", node.getApplication(), agentList);
            ServerBuilder builder = new ServerBuilder();
            builder.addAgentInfo(agentList);
            ServerInstanceList serverInstanceList = builder.build();
            // destination이 WAS이고 agent가 설치되어있으면 agentSet이 존재한다.
            node.setServerInstanceList(serverInstanceList);
        }

    }



    public static interface ResponseDataSource {
        ResponseHistogramSummary getResponseHistogramSummary(Application application);
    }

    public void appendResponseTime(final Range range, final MapResponseDao mapResponseDao) {
        appendResponseTime(new ResponseDataSource() {
            @Override
            public ResponseHistogramSummary getResponseHistogramSummary(Application application) {
                final List<ResponseTime> responseHistogram = mapResponseDao.selectResponseTime(application, range);
                final ResponseHistogramSummary histogramSummary = new ResponseHistogramSummary(application, range, responseHistogram);
                return histogramSummary;
            }
        });
    }

    public void appendResponseTime(final MapResponseHistogramSummary mapHistogramSummary) {
        appendResponseTime(new ResponseDataSource() {
            @Override
            public ResponseHistogramSummary getResponseHistogramSummary(Application application) {
                List<ResponseTime> responseHistogram = mapHistogramSummary.getResponseTimeList(application);
                final ResponseHistogramSummary histogramSummary = new ResponseHistogramSummary(application, range, responseHistogram);
                return histogramSummary;
            }
        });
    }

    public void appendResponseTime(ResponseDataSource responseDataSource) {
        if (responseDataSource == null) {
            throw new NullPointerException("responseDataSource must not be null");
        }

        final Collection<Node> nodes = this.nodeList.getNodeList();
        for (Node node : nodes) {
            if (node.getServiceType().isWas()) {
                // was일 경우 자신의 response 히스토그램을 조회하여 채운다.
                final Application nodeApplication = node.getApplication();
                final ResponseHistogramSummary nodeHistogramSummary = responseDataSource.getResponseHistogramSummary(nodeApplication);
                node.setResponseHistogramSummary(nodeHistogramSummary);
            } else if(node.getServiceType().isTerminal() || node.getServiceType().isUnknown()) {
                // 터미널 노드인경우, 자신을 가리키는 link값을 합하여 histogram을 생성한다.
                final Application nodeApplication = node.getApplication();
                final ResponseHistogramSummary nodeHistogramSummary = new ResponseHistogramSummary(nodeApplication, range);

                final List<Link> toLinkList = linkList.findToLink(nodeApplication);
                for (Link link : toLinkList) {
                    nodeHistogramSummary.addHistogram(link.getHistogram());
                }


                LinkCallDataMap linkCallDataMap = new LinkCallDataMap();
                for (Link link : toLinkList) {
                    LinkCallDataMap sourceLinkCallDataMap = link.getSourceLinkCallDataMap();
                    linkCallDataMap.addLinkDataMap(sourceLinkCallDataMap);
                }
                ApplicationTimeSeriesHistogramBuilder builder = new ApplicationTimeSeriesHistogramBuilder(nodeApplication, range);
                ApplicationTimeSeriesHistogram applicationTimeSeriesHistogram = builder.build(linkCallDataMap.getRawCallDataMap());
                nodeHistogramSummary.setApplicationTimeSeriesHistogram(applicationTimeSeriesHistogram);

                node.setResponseHistogramSummary(nodeHistogramSummary);
            } else if (node.getServiceType().isUser()) {
                // User노드인 경우 source 링크를 찾아 histogram을 생성한다.
                Application nodeApplication = node.getApplication();
                final ResponseHistogramSummary nodeHistogramSummary = new ResponseHistogramSummary(nodeApplication, range);
                final List<Link> fromLink = linkList.findFromLink(nodeApplication);
                if (fromLink.size() > 1) {
                    logger.warn("Invalid from UserNode:{}", linkList);
                    throw new IllegalArgumentException("Invalid from UserNode.size() :" + fromLink.size());
                } else if (fromLink.size() == 0) {
                    logger.warn("from UserNode not found:{}", nodeApplication);
                    continue;
                }
                final Link sourceLink = fromLink.get(0);
                nodeHistogramSummary.addHistogram(sourceLink.getHistogram());

                ApplicationTimeSeriesHistogram histogramData = sourceLink .getTargetApplicationTimeSeriesHistogramData();
                nodeHistogramSummary.setApplicationTimeSeriesHistogram(histogramData);

                node.setResponseHistogramSummary(nodeHistogramSummary);
            } else {
                // 그냥 데미 데이터
                Application nodeApplication = new Application(node.getApplication().getName(), node.getServiceType());
                ResponseHistogramSummary dummy = new ResponseHistogramSummary(nodeApplication, range);
                node.setResponseHistogramSummary(dummy);
            }

        }

    }

}
