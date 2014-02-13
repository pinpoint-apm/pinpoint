package com.nhn.pinpoint.web.applicationmap;

import java.util.*;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.applicationmap.rawdata.ResponseHistogram;
import com.nhn.pinpoint.web.dao.MapResponseDao;
import com.nhn.pinpoint.web.vo.*;
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

	private final Set<String> applicationNames = new HashSet<String>();

	private TimeSeriesStore timeSeriesStore;
	
	ApplicationMap() {
	}


	public List<Node> getNodes() {
		return this.nodeList.getNodeList();
	}

	public List<Link> getLinks() {
		return this.linkList.getLinks();
	}

	void indexingNode() {
        this.nodeList.markSequence();
	}

	Node findApplication(Application applicationId) {
        return this.nodeList.find(applicationId);
	}

    void addNode(List<Node> nodeList) {
        for (Node node : nodeList) {
            this.addNodeName(node);
        }
        this.nodeList.buildApplication(nodeList);
    }

	void addNodeName(Node node) {
		if (!node.getServiceType().isRpcClient()) {
			applicationNames.add(node.getApplicationName());
		}

	}

    void addLink(List<Link> relationList) {
        linkList.buildLink(relationList);
    }


	public TimeSeriesStore getTimeSeriesStore() {
		return timeSeriesStore;
	}

	public void setTimeSeriesStore(TimeSeriesStore timeSeriesStore) {
		this.timeSeriesStore = timeSeriesStore;
	}

    public void buildNode() {
        this.nodeList.build();
    }

    public boolean containsApplicationName(String applicationName) {
        return applicationNames.contains(applicationName);
    }

    public static interface ResponseDataSource {
        ResponseHistogramSummary getResponseHistogramSummary(Application application);
    }

    public void appendResponseTime(final Range range, final MapResponseDao mapResponseDao) {
        appendResponseTime(new ResponseDataSource() {
            @Override
            public ResponseHistogramSummary getResponseHistogramSummary(Application application) {
                final List<RawResponseTime> responseHistogram = mapResponseDao.selectResponseTime(application, range);
                ResponseHistogramSummary histogramSummary = createHistogramSummary(application, responseHistogram);

                Map<String, ResponseHistogram> agentHistogram = createAgentHistogram(application, responseHistogram);
                histogramSummary.setAgentHistogram(agentHistogram);
                logger.debug("agentHistogram:{}", agentHistogram);
                return histogramSummary;
            }
        });
    }

    public void appendResponseTime(final Map<Application, ResponseHistogramSummary> histogramSummaryMap) {
        appendResponseTime(new ResponseDataSource() {
            @Override
            public ResponseHistogramSummary getResponseHistogramSummary(Application application) {
                return histogramSummaryMap.get(application);
            }
        });
    }

    public void appendResponseTime(ResponseDataSource responseDataSource) {
        if (responseDataSource == null) {
            throw new NullPointerException("responseDataSource must not be null");
        }

        final List<Node> nodes = this.nodeList.getNodeList();
        for (Node node : nodes) {
            if (node.getServiceType().isWas()) {
                // was일 경우 자신의 response 히스토그램을 조회하여 채운다.
                final Application application = new Application(node.getApplicationName(), node.getServiceType());
                ResponseHistogramSummary histogramSummary = responseDataSource.getResponseHistogramSummary(application);
                node.setResponseHistogramSummary(histogramSummary);
            } else if(node.getServiceType().isTerminal() || node.getServiceType().isUnknown()) {
                // 터미널 노드인경우, 자신을 가리키는 link값을 합하여 histogram을 생성한다.
                Application nodeApplication = new Application(node.getApplicationName(), node.getServiceType());
                final ResponseHistogramSummary summary = new ResponseHistogramSummary(nodeApplication);

                List<Link> linkList = this.linkList.getLinks();
                for (Link link : linkList) {
                    Node toNode = link.getTo();
                    String applicationName = toNode.getApplicationName();
                    ServiceType serviceType = toNode.getServiceType();
                    Application destination = new Application(applicationName, serviceType);
                    // destnation이 자신을 가리킨다면 데이터를 머지함.
                    if (nodeApplication.equals(destination)) {
                        ResponseHistogram linkHistogram = link.getHistogram();
//                        summary.addTotal(linkHistogram);
                        summary.addLinkHistogram(linkHistogram);
                    }
                }
                node.setResponseHistogramSummary(summary);
            } else if(node.getServiceType().isUser()) {
                // User노드인 경우 source 링크를 찾아 histogram을 생성한다.
                Application nodeApplication = new Application(node.getApplicationName(), node.getServiceType());
                final ResponseHistogramSummary summary = new ResponseHistogramSummary(nodeApplication);

                List<Link> linkList = this.linkList.getLinks();
                for (Link link : linkList) {
                    Node fromNode = link.getFrom();
                    String applicationName = fromNode.getApplicationName();
                    ServiceType serviceType = fromNode.getServiceType();
                    Application source = new Application(applicationName, serviceType);
                    // destnation이 자신을 가리킨다면 데이터를 머지함.
                    if (nodeApplication.equals(source)) {
                        ResponseHistogram linkHistogram = link.getHistogram();
//                        summary.addTotal(linkHistogram);
                        summary.addLinkHistogram(linkHistogram);
                    }
                }
                node.setResponseHistogramSummary(summary);
            } else {
                // 그냥 데미 데이터
                Application nodeApplication = new Application(node.getApplicationName(), node.getServiceType());
                ResponseHistogramSummary dummy = new ResponseHistogramSummary(nodeApplication);
                node.setResponseHistogramSummary(dummy);
            }

        }

    }

    private ResponseHistogramSummary createHistogramSummary(Application application, List<RawResponseTime> responseHistogram) {
        final ResponseHistogramSummary summary = new ResponseHistogramSummary(application);
        for (RawResponseTime rawResponseTime : responseHistogram) {
            final List<ResponseHistogram> responseHistogramList = rawResponseTime.getResponseHistogramList();
            for (ResponseHistogram histogram : responseHistogramList) {
                summary.addTotal(histogram);
            }
        }
        return summary;
    }

    private Map<String, ResponseHistogram> createAgentHistogram(Application application, List<RawResponseTime> responseHistogram) {
        // 타입을 좀더 정확히 agentId + serviceType으로 해야 될듯하다.
        Map<String, ResponseHistogram> agentHistogramSummary = new HashMap<String, ResponseHistogram>();
        for (RawResponseTime rawResponseTime : responseHistogram) {
            Set<Map.Entry<String, ResponseHistogram>> agentHistogramEntry = rawResponseTime.getAgentHistogram();
            for (Map.Entry<String, ResponseHistogram> entry : agentHistogramEntry) {
                ResponseHistogram agentHistogram = agentHistogramSummary.get(entry.getKey());
                if (agentHistogram == null) {
                    agentHistogram = new ResponseHistogram(application.getServiceType());
                    agentHistogramSummary.put(entry.getKey(), agentHistogram);
                }
                agentHistogram.add(entry.getValue());
            }
        }
        return agentHistogramSummary;
    }
}
