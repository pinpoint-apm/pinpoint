package com.nhn.pinpoint.web.applicationmap.rawdata;

import java.util.*;

import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.LinkKey;

public class LinkDataMap {
    private final Map<LinkKey, LinkData> linkDataMap = new HashMap<LinkKey, LinkData>();

	public LinkDataMap() {
	}

    public LinkDataMap(LinkDataMap copyLinkDataMap) {
        if (copyLinkDataMap == null) {
            throw new NullPointerException("copyLinkDataMap must not be null");
        }
        addLinkDataMap(copyLinkDataMap);
    }

    public Collection<LinkData> getLinkDataList() {
        return linkDataMap.values();
    }

    public void addLinkData(Application sourceApplication, String sourceAgentId, Application destinationApplication, String destinationAgentId, long timestamp, short slotTime, long count) {
        final LinkData linkData = getLinkData(sourceApplication, destinationApplication);
        linkData.addLinkData(sourceAgentId, sourceApplication.getServiceTypeCode(), destinationAgentId, destinationApplication.getServiceTypeCode(), timestamp, slotTime, count);
    }


	@Override
	public String toString() {
		return "LinkDataMap [" + linkDataMap + "]";
	}

    public void addLinkDataMap(LinkDataMap linkDataMap) {
        if (linkDataMap == null) {
            throw new NullPointerException("linkDataMap must not be null");
        }
        for (LinkData copyLinkData : linkDataMap.linkDataMap.values()) {
            addLinkData(copyLinkData);
        }
    }

    public void addLinkData(LinkData copyLinkData) {
        if (copyLinkData == null) {
            throw new NullPointerException("copyLinkData must not be null");
        }
        Application fromApplication = copyLinkData.getFromApplication();
        Application toApplication = copyLinkData.getToApplication();
        LinkData linkData = getLinkData(fromApplication, toApplication);
        linkData.add(copyLinkData);
    }

    private LinkData getLinkData(Application fromApplication, Application toApplication) {
        final LinkKey key = new LinkKey(fromApplication, toApplication);
        LinkData findLink = linkDataMap.get(key);
        if (findLink == null) {
            findLink = new LinkData(fromApplication, toApplication);
            linkDataMap.put(key, findLink);
        }
        return findLink;
    }

    public int size() {
        return linkDataMap.size();
    }

    public LinkData getLinkData(LinkKey findLinkKey) {
        if (findLinkKey == null) {
            throw new NullPointerException("findLinkKey must not be null");
        }
        return this.linkDataMap.get(findLinkKey);
    }
}
