package com.nhn.pinpoint.web.applicationmap.rawdata;

import java.util.HashSet;
import java.util.Set;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.vo.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DB에서 조회한 application호출 관계 정보.
 * 
 * @author netspider
 * @author emeroad
 */
public class LinkStatistics {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private final Application fromApplication;
    private Application toApplication;

    private final RawCallDataMap callDataMap;

    private final Set<AgentInfoBo> toAgentSet;

	public LinkStatistics(Application fromApplication, Application toApplication) {
        if (fromApplication == null) {
            throw new NullPointerException("fromAppliation must not be null");
        }
        if (toApplication == null) {
            throw new NullPointerException("toApplication must not be null");
        }
        this.fromApplication = fromApplication;
		this.toApplication = toApplication;

        this.toAgentSet = new HashSet<AgentInfoBo>();

        this.callDataMap = new RawCallDataMap();
	}

    /**
	 * 
	 * @param hostname
	 *            host이름 또는 endpoint
	 * @param slot
	 * @param value
	 */
	public void addCallData(String callerAgentId, short callerServiceTypeCode, String hostname, short serviceTypeCode, long timestamp, short slot, long value) {
		// TODO 임시코드
		if (hostname == null || hostname.length() == 0) {
			hostname = "UNKNOWNHOST";
		}
        this.callDataMap.addCallData(callerAgentId, callerServiceTypeCode, hostname, serviceTypeCode, timestamp, slot, value);
	}


    public Application getFromApplication() {
        return this.fromApplication;
    }

    public Application getToApplication() {
//        if (toAcceptApplication != null) {
//            return toAcceptApplication;
//        }
        return this.toApplication;
    }

    public void setToAcceptApplication(Application toAcceptApplication) {
        this.toApplication = toAcceptApplication;
    }

//    public Application getToAcceptApplication() {
//        return toAcceptApplication;
//    }

    public RawCallDataMap getCallDataMap() {
        return  this.callDataMap;
    }

    public CallHistogramList getTargetList() {
        return callDataMap.getTargetList();
	}

    public CallHistogramList getSourceList() {
        return callDataMap.getSourceList();
    }

	public Set<AgentInfoBo> getToAgentSet() {
		return toAgentSet;
	}

    public void addToAgentSet(Set<AgentInfoBo> agentSet) {
        if (agentSet == null) {
            throw new NullPointerException("agentSet must not be null");
        }
        this.toAgentSet.addAll(agentSet);
    }

	public void add(final LinkStatistics applicationStatistics) {
        if (applicationStatistics == null) {
            throw new NullPointerException("applicationStatistics must not be null");
        }
        if (!this.equals(applicationStatistics)) {
            throw new IllegalArgumentException("Can't merge with different link.");
		}
        final RawCallDataMap target = applicationStatistics.callDataMap;
        this.callDataMap.addCallData(target);
        this.toAgentSet.addAll(applicationStatistics.toAgentSet);
	}

    @Override
    public String toString() {
        return "LinkStatistics{" +
                "fromApplication=" + fromApplication +
                ", toApplication=" + toApplication +
                ", " + callDataMap +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkStatistics that = (LinkStatistics) o;

        if (!fromApplication.equals(that.fromApplication)) return false;
        if (!toApplication.equals(that.toApplication)) return false;
//        if (toAcceptApplication != null) {
//            if (!toAcceptApplication.equals(that.toAcceptApplication)) return false;
//        } else {
//            if (!toApplication.equals(that.toApplication)) return false;
//        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = fromApplication.hashCode();
        result = 31 * result + toApplication.hashCode();
//        if (toAcceptApplication != null) {
//            result = 31 * result + toAcceptApplication.hashCode();
//        } else {
//            result = 31 * result + toApplication.hashCode();
//        }
        return result;
    }
}
