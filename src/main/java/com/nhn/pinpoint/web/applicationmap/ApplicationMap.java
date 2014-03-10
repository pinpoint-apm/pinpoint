package com.nhn.pinpoint.web.applicationmap;

import java.util.*;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;
import com.nhn.pinpoint.web.dao.MapResponseDao;
import com.nhn.pinpoint.web.vo.*;
import org.codehaus.jackson.map.ObjectMapper;
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
    private final Range range;


	ApplicationMap(Range range) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        this.range = range;
	}


    public Collection<Node> getNodes() {
		return this.nodeList.getNodeList();
	}

	public Collection<Link> getLinks() {
		return this.linkList.getLinks();
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
                final Application application = new Application(node.getApplicationName(), node.getServiceType());
                ResponseHistogramSummary nodeHistogramSummary = responseDataSource.getResponseHistogramSummary(application);
                node.setResponseHistogramSummary(nodeHistogramSummary);
            } else if(node.getServiceType().isTerminal() || node.getServiceType().isUnknown()) {
                // 터미널 노드인경우, 자신을 가리키는 link값을 합하여 histogram을 생성한다.
                Application nodeApplication = new Application(node.getApplicationName(), node.getServiceType());
                final ResponseHistogramSummary nodeHistogramSummary = new ResponseHistogramSummary(nodeApplication, range);

                Collection<Link> linkList = this.linkList.getLinks();
                for (Link link : linkList) {
                    Node toNode = link.getTo();
                    String applicationName = toNode.getApplicationName();
                    ServiceType serviceType = toNode.getServiceType();
                    Application destination = new Application(applicationName, serviceType);
                    // destnation이 자신을 가리킨다면 데이터를 머지함.
                    if (nodeApplication.equals(destination)) {
                        Histogram linkHistogram = link.getHistogram();
//                        summary.addApplicationLevelHistogram(linkHistogram);
                        nodeHistogramSummary.addLinkHistogram(linkHistogram);
                    }
                }
                node.setResponseHistogramSummary(nodeHistogramSummary);
            } else if(node.getServiceType().isUser()) {
                // User노드인 경우 source 링크를 찾아 histogram을 생성한다.
                Application nodeApplication = new Application(node.getApplicationName(), node.getServiceType());
                final ResponseHistogramSummary nodeHistogramSummary = new ResponseHistogramSummary(nodeApplication, range);

                Collection<Link> linkList = this.linkList.getLinks();
                for (Link link : linkList) {
                    Node fromNode = link.getFrom();
                    String applicationName = fromNode.getApplicationName();
                    ServiceType serviceType = fromNode.getServiceType();
                    Application source = new Application(applicationName, serviceType);
                    // destnation이 자신을 가리킨다면 데이터를 머지함.
                    if (nodeApplication.equals(source)) {
                        Histogram linkHistogram = link.getHistogram();
//                        summary.addApplicationLevelHistogram(linkHistogram);
                        nodeHistogramSummary.addLinkHistogram(linkHistogram);
                    }
                }
                node.setResponseHistogramSummary(nodeHistogramSummary);
            } else {
                // 그냥 데미 데이터
                Application nodeApplication = new Application(node.getApplicationName(), node.getServiceType());
                ResponseHistogramSummary dummy = new ResponseHistogramSummary(nodeApplication, range);
                node.setResponseHistogramSummary(dummy);
            }

        }

    }

}
