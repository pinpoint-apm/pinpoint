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
public class LinkData {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Application fromApplication;
    private final Application toApplication;

    private final LinkCallDataMap linkCallDataMap;

	public LinkData(Application fromApplication, Application toApplication) {
        if (fromApplication == null) {
            throw new NullPointerException("fromApplication must not be null");
        }
        if (toApplication == null) {
            throw new NullPointerException("toApplication must not be null");
        }

        this.fromApplication = fromApplication;
		this.toApplication = toApplication;

        this.linkCallDataMap = new LinkCallDataMap();
	}

    // 이건 일부러 복사 생성자로 구현안함.
    public LinkData(Application fromApplication, Application toApplication, LinkCallDataMap linkCallDataMap) {
        if (fromApplication == null) {
            throw new NullPointerException("fromApplication must not be null");
        }
        if (toApplication == null) {
            throw new NullPointerException("toApplication must not be null");
        }
        this.fromApplication = fromApplication;
        this.toApplication = toApplication;

        this.linkCallDataMap = linkCallDataMap;
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
        this.linkCallDataMap.addCallData(callerAgentId, callerServiceTypeCode, hostname, serviceTypeCode, timestamp, slot, value);
	}


    public Application getFromApplication() {
        return this.fromApplication;
    }

    public Application getToApplication() {
        return this.toApplication;
    }


    public LinkCallDataMap getLinkCallDataMap() {
        return  this.linkCallDataMap;
    }

    public CallHistogramList getTargetList() {
        return linkCallDataMap.getTargetList();
	}

    public CallHistogramList getSourceList() {
        return linkCallDataMap.getSourceList();
    }

	public void add(final LinkData applicationStatistics) {
        if (applicationStatistics == null) {
            throw new NullPointerException("applicationStatistics must not be null");
        }
        if (!this.equals(applicationStatistics)) {
            throw new IllegalArgumentException("Can't merge with different link.");
		}
        final LinkCallDataMap target = applicationStatistics.linkCallDataMap;
        this.linkCallDataMap.addLinkDataMap(target);
	}

    @Override
    public String toString() {
        return "LinkData{" +
                "fromApplication=" + fromApplication +
                ", toApplication=" + toApplication +
                ", " + linkCallDataMap +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkData that = (LinkData) o;

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
