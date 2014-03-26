package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.common.ServiceType;
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

    private final LinkKey linkKey;

    private final Node fromNode;
    private final Node toNode;
    private final Range range;

    private static final LinkStateResolver linkStateResolver = new LinkStateResolver();

    private final RawCallDataMap source;

    private final RawCallDataMap target;

    private static final ObjectMapper MAPPER = new ObjectMapper();


    public Link(Node from, Node to, Range range, RawCallDataMap source, RawCallDataMap target) {
        this(createLinkKey(from, to), from, to, range, source, target);

    }

    private static LinkKey createLinkKey(Node from, Node to) {
        if (from == null) {
            throw new NullPointerException("fromNode must not be null");
        }
        if (to == null) {
            throw new NullPointerException("toNode must not be null");
        }
        final Application fromApplication = from.getApplication();
        final Application toApplication = to.getApplication();
        return new LinkKey(fromApplication, toApplication);
    }

    Link(LinkKey linkKey, Node fromNode, Node toNode, Range range, RawCallDataMap source, RawCallDataMap target) {
        if (fromNode == null) {
            throw new NullPointerException("fromNode must not be null");
        }
        if (toNode == null) {
            throw new NullPointerException("toNode must not be null");
        }
        if (linkKey == null) {
            throw new NullPointerException("linkKey must not be null");
        }
        if (source == null) {
            throw new NullPointerException("source must not be null");
        }
        if (target == null) {
            throw new NullPointerException("target must not be null");
        }

        this.linkKey = linkKey;
        this.fromNode = fromNode;
        this.toNode = toNode;

        this.range = range;

        this.source = new RawCallDataMap(source);
        this.target = new RawCallDataMap(target);
    }

    public Link(Link copyLink) {
        if (copyLink == null) {
            throw new NullPointerException("copyLink must not be null");
        }
        this.linkKey = copyLink.linkKey;
        this.fromNode = copyLink.fromNode;
        this.toNode = copyLink.toNode;

        this.range = copyLink.range;

        this.source = new RawCallDataMap(copyLink.source);
        this.target = new RawCallDataMap(copyLink.target);
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
		return linkKey;
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

    @JsonIgnore
    public String getJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

	public CallHistogramList getTargetList() {
		return source.getTargetList();
	}


	public Histogram getHistogram() {
        // 내가 호출하는 대상의 serviceType을 가져와야 한다.
        // tomcat -> arcus를 호출한다고 하였을 경우 arcus의 타입을 가져와야함.
        final Histogram linkHistogram = new Histogram(toNode.getServiceType());
        for (CallHistogram callHistogram : source.getTargetList().getCallHistogramList()) {
            linkHistogram.add(callHistogram.getHistogram());
        }
		return linkHistogram;
	}

    public Histogram getTargetHistogram() {
        // 내가 호출하는 대상의 serviceType을 가져와야 한다.
        // tomcat -> arcus를 호출한다고 하였을 경우 arcus의 타입을 가져와야함.
        final Histogram linkHistogram = new Histogram(toNode.getServiceType());
        for (CallHistogram callHistogram : target.getTargetList().getCallHistogramList()) {
            linkHistogram.add(callHistogram.getHistogram());
        }
        return linkHistogram;
    }

    @JsonIgnore
    public CallHistogramList getSourceList() {
        return source.getSourceList();
    }

    public List<ResponseTimeViewModel> getSourceApplicationTimeSeriesHistogram() {
        // form인것 같지만 link의 시간은 rpc를 기준으로 삼아야 하기 때문에. to를 기준으로 삼아야 한다.
        ApplicationTimeSeriesHistogramBuilder builder = new ApplicationTimeSeriesHistogramBuilder(toNode.getApplication(), range);
        ApplicationTimeSeriesHistogram applicationTimeSeriesHistogram = builder.build(source.getRawCallDataMap());
        List<ResponseTimeViewModel> viewModel = applicationTimeSeriesHistogram.createViewModel();
        return viewModel;
    }

    public AgentResponseTimeViewModelList getSourceAgentTimeSeriesHistogram() {

        // form인것 같지만 link의 시간은 rpc를 기준으로 삼아야 하기 때문에. to를 기준으로 삼아야 한다.
        AgentTimeSeriesHistogramBuilder builder = new AgentTimeSeriesHistogramBuilder(toNode.getApplication(), range);
        AgentTimeSeriesHistogram applicationTimeSeriesHistogram = builder.build(source.getRawCallDataMap());
        AgentResponseTimeViewModelList agentResponseTimeViewModelList = new AgentResponseTimeViewModelList(applicationTimeSeriesHistogram.createViewModel());
        return agentResponseTimeViewModelList;
    }

    public String getLinkState() {
        // 이거 호출할때 마다 생성해서 수정이 요망함.
        Histogram histogram = getHistogram();
        return linkStateResolver.resolve(this, histogram);
    }

	public void addLink(Link link) {
        if (link == null) {
            throw new NullPointerException("link must not be null");
        }
        // TODO this.equals로 바꿔도 되지 않을까?
		if (!(this.fromNode.equals(link.getFrom()) && this.toNode.equals(link.getTo()))) {
            logger.info("fromNode:{}, to:{}, fromNode:{}, linkTo:{}", fromNode, toNode, link.getFrom(), link.getTo());
            throw new IllegalArgumentException("Can't merge.");
        }
        this.source.addCallData(link.source);

        this.target.addCallData(link.target);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((linkKey == null) ? 0 : linkKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Link other = (Link) obj;
		if (linkKey == null) {
			if (other.linkKey != null)
				return false;
		} else if (!linkKey.equals(other.linkKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Link [linkKey=" + linkKey + ", fromNode=" + fromNode + ", toNode=" + toNode + ", source=" + source + "]";
	}

}
