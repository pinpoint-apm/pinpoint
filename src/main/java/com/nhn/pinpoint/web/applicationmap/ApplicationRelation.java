package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.web.applicationmap.rawdata.Host;
import com.nhn.pinpoint.web.applicationmap.rawdata.HostList;
import com.nhn.pinpoint.web.applicationmap.rawdata.ResponseHistogram;
import com.nhn.pinpoint.web.service.ComplexNodeId;
import com.nhn.pinpoint.web.service.NodeId;
import com.nhn.pinpoint.web.service.SimpleNodeId;
import com.nhn.pinpoint.web.util.Mergeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * application map에서 application간의 관계를 담은 클래스
 * 
 * @author netspider
 * @author emeroad
 */
public class ApplicationRelation {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final NodeId id;

    private final Application from;
    private final Application to;
	private HostList hostList;


    public ApplicationRelation(Application from, Application to, HostList hostList) {
        this(createKey(from, to), from, to, hostList);

    }

    private static ComplexNodeId createKey(Application from, Application to) {
        if (from == null) {
            throw new NullPointerException("from must not be null");
        }
        if (to == null) {
            throw new NullPointerException("to must not be null");
        }
        SimpleNodeId fromId = (SimpleNodeId) from.getId();
        SimpleNodeId toId = (SimpleNodeId) to.getId();
        return new ComplexNodeId(fromId.getKey(), toId.getKey());
    }

    ApplicationRelation(NodeId id, Application from, Application to, HostList hostList) {
        if (from == null) {
            throw new NullPointerException("from must not be null");
        }
        if (to == null) {
            throw new NullPointerException("to must not be null");
        }
        if (id == null) {
            throw new NullPointerException("id must not be null");
        }
        this.id = id;
        this.from = from;
        this.to = to;
        this.hostList = hostList;
    }

	public NodeId getId() {
		return id;
	}

	public Application getFrom() {
		return from;
	}

	public Application getTo() {
		return to;
	}

	public HostList getHostList() {
		return hostList;
	}


	public ResponseHistogram getHistogram() {
		ResponseHistogram result = null;

		for (Host host : hostList.getHostList()) {
			if (result == null) {
				// FIXME 뭔가 괴상한 방식이긴 하지만..
				ResponseHistogram histogram = host.getHistogram();
				result = new ResponseHistogram(histogram.getServiceType());
			}
			result.add(host.getHistogram());
		}
		return result;
	}

	public ApplicationRelation add(ApplicationRelation relation) {
		// TODO this.equals로 바꿔도 되지 않을까?
		if (this.from.equals(relation.getFrom()) && this.to.equals(relation.getTo())) {
			// TODO Mergable value map을 만들어야 하나...
            HostList relationHostList = relation.getHostList();
            this.hostList.addHostList(relationHostList);
		} else {
            logger.info("from:{}, to:{}, relationFrom:{}, relationTo:{}", from, to, relation.getFrom(), relation.getTo());
			throw new IllegalArgumentException("Can't merge.");
		}
		return this;
	}

    public ApplicationRelation deepCopy() {
        HostList copyHost = this.hostList.deepCopy();
        ApplicationRelation copy = new ApplicationRelation(this.id, this.from, this.to, copyHost);
        return copy;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		ApplicationRelation other = (ApplicationRelation) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ApplicationRelation [id=" + id + ", from=" + from + ", to=" + to + ", hostList=" + hostList + "]";
	}

}
