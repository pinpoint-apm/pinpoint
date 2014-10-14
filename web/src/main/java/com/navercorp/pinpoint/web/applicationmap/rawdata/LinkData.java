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

    private LinkCallDataMap linkCallDataMap;

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
	 * @param count
	 */
	public void addLinkData(String callerAgentId, short callerServiceTypeCode, String hostname, short serviceTypeCode, long timestamp, short slot, long count) {
        if (hostname == null) {
            throw new NullPointerException("hostname must not be null");
        }
        this.linkCallDataMap.addCallData(callerAgentId, callerServiceTypeCode, hostname, serviceTypeCode, timestamp, slot, count);
	}

    public void resetLinkData() {
        this.linkCallDataMap = new LinkCallDataMap();
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

    public AgentHistogramList getTargetList() {
        return linkCallDataMap.getTargetList();
	}

    public AgentHistogramList getSourceList() {
        return linkCallDataMap.getSourceList();
    }

	public void add(final LinkData linkData) {
        if (linkData == null) {
            throw new NullPointerException("linkData must not be null");
        }
        if (!this.equals(linkData)) {
            throw new IllegalArgumentException("Can't merge with different link.");
		}
        final LinkCallDataMap target = linkData.linkCallDataMap;
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
