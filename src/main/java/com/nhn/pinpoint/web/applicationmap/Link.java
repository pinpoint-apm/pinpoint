package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.web.applicationmap.rawdata.CallHistogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.CallHistogramList;
import com.nhn.pinpoint.web.applicationmap.rawdata.Histogram;
import com.nhn.pinpoint.web.vo.LinkKey;
import com.nhn.pinpoint.web.vo.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * application map에서 application간의 관계를 담은 클래스
 * 
 * @author netspider
 * @author emeroad
 */
public class Link {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LinkKey linkKey;

    private final Node fromNode;
    private final Node toNode;

	private final CallHistogramList tagetList;
    private CallHistogramList sourceList;


    public Link(Node from, Node to, CallHistogramList tagetList) {
        this(createLinkKey(from, to), from, to, tagetList);

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

    Link(LinkKey linkKey, Node fromNode, Node toNode, CallHistogramList tagetList) {
        if (fromNode == null) {
            throw new NullPointerException("fromNode must not be null");
        }
        if (toNode == null) {
            throw new NullPointerException("toNode must not be null");
        }
        if (linkKey == null) {
            throw new NullPointerException("linkKey must not be null");
        }
        this.linkKey = linkKey;
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.tagetList = tagetList;
    }

    public Link(Link copyLink) {
        if (copyLink == null) {
            throw new NullPointerException("copyLink must not be null");
        }
        this.linkKey = copyLink.linkKey;
        this.fromNode = copyLink.fromNode;
        this.toNode = copyLink.toNode;
        this.tagetList = new CallHistogramList(copyLink.tagetList);
        this.sourceList = new CallHistogramList(copyLink.sourceList);
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

	public CallHistogramList getTargetList() {
		return tagetList;
	}


	public Histogram getHistogram() {
		Histogram result = null;

		for (CallHistogram callHistogram : tagetList.getCallHistogramList()) {
			if (result == null) {
				// FIXME 뭔가 괴상한 방식이긴 하지만..
				Histogram histogram = callHistogram.getHistogram();
				result = new Histogram(histogram.getServiceType());
			}
			result.add(callHistogram.getHistogram());
		}
		return result;
	}

    public void setSourceList(CallHistogramList sourceList) {
        this.sourceList = sourceList;
    }

    public CallHistogramList getSourceList() {
        return sourceList;
    }

	public void addLink(Link link) {
        if (link == null) {
            throw new NullPointerException("link must not be null");
        }
        // TODO this.equals로 바꿔도 되지 않을까?
		if (this.fromNode.equals(link.getFrom()) && this.toNode.equals(link.getTo())) {
            logger.info("fromNode:{}, to:{}, fromNode:{}, linkTo:{}", fromNode, toNode, link.getFrom(), link.getTo());
            throw new IllegalArgumentException("Can't merge.");
        }

        CallHistogramList linkCallHistogramList = link.getTargetList();
        this.tagetList.addHostList(linkCallHistogramList);
        this.sourceList.addHostList(link.getSourceList());
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
		return "Link [linkKey=" + linkKey + ", fromNode=" + fromNode + ", toNode=" + toNode + ", tagetList=" + tagetList + "]";
	}

}
