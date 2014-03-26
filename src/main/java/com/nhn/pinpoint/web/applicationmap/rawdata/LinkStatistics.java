package com.nhn.pinpoint.web.applicationmap.rawdata;


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

	public LinkStatistics(Application fromApplication, Application toApplication) {
        if (fromApplication == null) {
            throw new NullPointerException("fromApplication must not be null");
        }
        if (toApplication == null) {
            throw new NullPointerException("toApplication must not be null");
        }
        this.fromApplication = fromApplication;
		this.toApplication = toApplication;

        this.callDataMap = new RawCallDataMap();
	}

    // 이건 일부러 복사 생성자로 구현안함.
    public LinkStatistics(Application fromApplication, Application toApplication, RawCallDataMap callDataMap) {
        if (fromApplication == null) {
            throw new NullPointerException("fromApplication must not be null");
        }
        if (toApplication == null) {
            throw new NullPointerException("toApplication must not be null");
        }
        this.fromApplication = fromApplication;
        this.toApplication = toApplication;

        this.callDataMap = callDataMap;
    }

    /**
	 * 
	 * @param hostname
	 *            host이름 또는 endpoint
	 * @param slot
	 * @param value
	 */
	public void addLinkData(String callerAgentId, short callerServiceTypeCode, String hostname, short serviceTypeCode, long timestamp, short slot, long value) {
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
        return this.toApplication;
    }


    public RawCallDataMap getCallDataMap() {
        return  this.callDataMap;
    }

    public CallHistogramList getTargetList() {
        return callDataMap.getTargetList();
	}

    public CallHistogramList getSourceList() {
        return callDataMap.getSourceList();
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

        return true;
    }

    @Override
    public int hashCode() {
        int result = fromApplication.hashCode();
        result = 31 * result + toApplication.hashCode();
        return result;
    }
}
