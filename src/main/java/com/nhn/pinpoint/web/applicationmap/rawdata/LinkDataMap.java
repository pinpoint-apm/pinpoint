package com.nhn.pinpoint.web.applicationmap.rawdata;

import java.util.*;

import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.LinkKey;

public class LinkDataMap {

    private final Map<LinkKey, LinkData> linkStatData = new HashMap<LinkKey, LinkData>();

	public LinkDataMap() {
	}

    public Collection<LinkData> getLinkStatData() {
        return linkStatData.values();
    }

    public void addLinkData(Application srcApplication, String srcAgentId, Application destinationApplication, String destinationAgentId, long timestamp, short slotTime, long value) {
        final LinkData linkStat = getLinkStatistics(srcApplication, destinationApplication);
        linkStat.addLinkData(srcAgentId, srcApplication.getServiceTypeCode(), destinationAgentId, destinationApplication.getServiceTypeCode(), timestamp, slotTime, value);
    }


	@Override
	public String toString() {
		return "LinkDataMap [linkStatData=" + linkStatData + "]";
	}

    public void addLinkStatisticsData(LinkDataMap linkDataMap) {
        if (linkDataMap == null) {
            throw new NullPointerException("linkDataMap must not be null");
        }
        for (LinkData copyLinkData : linkDataMap.linkStatData.values()) {
            addLinkStatistics(copyLinkData);
        }
    }

    public void addLinkStatistics(LinkData copyLinkData) {
        if (copyLinkData == null) {
            throw new NullPointerException("copyLinkData must not be null");
        }
        Application fromApplication = copyLinkData.getFromApplication();
        Application toApplication = copyLinkData.getToApplication();
        LinkData linkData = getLinkStatistics(fromApplication, toApplication);
        linkData.add(copyLinkData);
    }

    private LinkData getLinkStatistics(Application fromApplication, Application toApplication) {
        final LinkKey key = new LinkKey(fromApplication, toApplication);
        LinkData findLink = linkStatData.get(key);
        if (findLink == null) {
            findLink = new LinkData(fromApplication, toApplication);
            linkStatData.put(key, findLink);
        }
        return findLink;
    }

    public int size() {
        return linkStatData.size();
    }
}
