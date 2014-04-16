package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.applicationmap.histogram.*;
import com.nhn.pinpoint.web.applicationmap.rawdata.*;
import com.nhn.pinpoint.web.view.AgentResponseTimeViewModelList;
import com.nhn.pinpoint.web.view.LinkSerializer;
import com.nhn.pinpoint.web.view.ResponseTimeViewModel;
import com.nhn.pinpoint.web.vo.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * application map에서 application간의 관계를 담은 클래스
 * 
 * @author netspider
 * @author emeroad
 */
@JsonSerialize(using = LinkSerializer.class)
public class Link {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String LINK_DELIMITER = "~";

    // 링크를 생성한 데이터의 주체가 누구인가를 나타냄
    // source에 의해서 먼저 생성된것인지, target에 의해서 수동적으로 생성된것인지 나타낸다.
    private final CreateType createType;
    private final Node fromNode;
    private final Node toNode;

    private final Range range;

    private final LinkStateResolver linkStateResolver = LinkStateResolver.DEFAULT_LINK_STATE_RESOLVER;

    private final LinkCallDataMap sourceLinkCallDataMap = new LinkCallDataMap();

    private final LinkCallDataMap targetLinkCallDataMap = new LinkCallDataMap();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Histogram linkHistogram;



    public Link(CreateType createType, Node fromNode, Node toNode, Range range) {
        if (createType == null) {
            throw new NullPointerException("createType must not be null");
        }
        if (fromNode == null) {
            throw new NullPointerException("fromNode must not be null");
        }
        if (toNode == null) {
            throw new NullPointerException("toNode must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }

        this.createType = createType;

        this.fromNode = fromNode;
        this.toNode = toNode;

        this.range = range;

    }

    public Application getFilterApplication() {
        // User 링크일 경우 from을 보면 안되고 was를 봐야 한다.
        // User는 가상의 링크이기 때문에, User로 필터링을 칠수 없음.
        if (fromNode.getServiceType() == ServiceType.USER) {
            return toNode.getApplication();
        }
        return fromNode.getApplication();
    }


	public LinkKey getLinkKey() {
		return new LinkKey(fromNode.getApplication(), toNode.getApplication());
	}

	public Node getFrom() {
		return fromNode;
	}

	public Node getTo() {
		return toNode;
	}

    public String getLinkName() {
        return fromNode.getNodeName() + LINK_DELIMITER + toNode.getNodeName();
    }

    public LinkCallDataMap getSourceLinkCallDataMap() {
        return sourceLinkCallDataMap;
    }

    public LinkCallDataMap getTargetLinkCallDataMap() {
        return targetLinkCallDataMap;
    }

    @JsonIgnore
    public String getJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

	public AgentHistogramList getTargetList() {
		return sourceLinkCallDataMap.getTargetList();
	}


	public Histogram getHistogram() {
        if (linkHistogram == null) {
            linkHistogram = createHistogram0();
        }
        return linkHistogram;
	}

    private Histogram createHistogram0() {
        // 내가 호출하는 대상의 serviceType을 가져와야 한다.
        // tomcat -> arcus를 호출한다고 하였을 경우 arcus의 타입을 가져와야함.
        final Histogram linkHistogram = new Histogram(toNode.getServiceType());
        final LinkCallDataMap findMap = getLinkCallDataMap();
        AgentHistogramList targetList = findMap.getTargetList();
        return targetList.mergeHistogram(toNode.getServiceType());
    }

    private LinkCallDataMap getLinkCallDataMap() {
        switch (createType) {
            case Source:
                return sourceLinkCallDataMap;
            case Target:
                return targetLinkCallDataMap;
            default:
                throw new IllegalArgumentException("invalid CreateType:" + createType);
        }
    }

    public List<ResponseTimeViewModel> getLinkApplicationTimeSeriesHistogram() {
        if (createType == CreateType.Source)  {
            return getSourceApplicationTimeSeriesHistogram();
        } else {
            return getTargetApplicationTimeSeriesHistogram();
        }
    }

    public Histogram getTargetHistogram() {
        // 내가 호출하는 대상의 serviceType을 가져와야 한다.
        // tomcat -> arcus를 호출한다고 하였을 경우 arcus의 타입을 가져와야함.
        AgentHistogramList targetList = targetLinkCallDataMap.getTargetList();
        return targetList.mergeHistogram(toNode.getServiceType());

    }

    @JsonIgnore
    public AgentHistogramList getSourceList() {
        return sourceLinkCallDataMap.getSourceList();
    }

    public void addSource(LinkCallDataMap sourceLinkCallDataMap) {
        this.sourceLinkCallDataMap.addLinkDataMap(sourceLinkCallDataMap);
    }

    public void addTarget(LinkCallDataMap targetLinkCallDataMap) {
        this.targetLinkCallDataMap.addLinkDataMap(targetLinkCallDataMap);
    }

    public List<ResponseTimeViewModel> getSourceApplicationTimeSeriesHistogram() {
        ApplicationTimeHistogram histogramData = getSourceApplicationTimeSeriesHistogramData();
        return histogramData.createViewModel();

    }

    private ApplicationTimeHistogram getSourceApplicationTimeSeriesHistogramData() {
        // form인것 같지만 link의 시간은 rpc를 기준으로 삼아야 하기 때문에. to를 기준으로 삼아야 한다.
        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(toNode.getApplication(), range);
        return builder.build(sourceLinkCallDataMap.getLinkDataMap());
    }

    public ApplicationTimeHistogram getTargetApplicationTimeSeriesHistogramData() {
        // form인것 같지만 link의 시간은 rpc를 기준으로 삼아야 하기 때문에. to를 기준으로 삼아야 한다.
        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(toNode.getApplication(), range);
        return builder.build(targetLinkCallDataMap.getLinkDataMap());
    }

    public AgentResponseTimeViewModelList getSourceAgentTimeSeriesHistogram() {

        // form인것 같지만 link의 시간은 rpc를 기준으로 삼아야 하기 때문에. to를 기준으로 삼아야 한다.
        AgentTimeHistogramBuilder builder = new AgentTimeHistogramBuilder(toNode.getApplication(), range);
        AgentTimeHistogram applicationTimeSeriesHistogram = builder.buildSource(sourceLinkCallDataMap.getLinkDataMap());
        AgentResponseTimeViewModelList agentResponseTimeViewModelList = new AgentResponseTimeViewModelList(applicationTimeSeriesHistogram.createViewModel());
        return agentResponseTimeViewModelList;
    }

    public AgentTimeHistogram getTargetAgentTimeHistogram() {

        AgentTimeHistogramBuilder builder = new AgentTimeHistogramBuilder(toNode.getApplication(), range);
        AgentTimeHistogram agentTimeHistogram = builder.buildSource(targetLinkCallDataMap.getLinkDataMap());
        return agentTimeHistogram;
    }

    public List<ResponseTimeViewModel> getTargetApplicationTimeSeriesHistogram() {
        ApplicationTimeHistogram targetApplicationTimeHistogramData = getTargetApplicationTimeSeriesHistogramData();
        return targetApplicationTimeHistogramData.createViewModel();
    }


    public String getLinkState() {
        return linkStateResolver.resolve(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link link = (Link) o;

        if (!fromNode.equals(link.fromNode)) return false;
        if (!toNode.equals(link.toNode)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fromNode.hashCode();
        result = 31 * result + toNode.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Link{" +
                "from=" + fromNode +
                " -> to=" + toNode +
                '}';
    }
}
