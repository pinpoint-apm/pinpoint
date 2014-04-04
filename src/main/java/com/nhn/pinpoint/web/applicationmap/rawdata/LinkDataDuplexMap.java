package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.web.vo.LinkKey;

import java.util.Collection;

/**
 * @author emeroad
 */
public class LinkDataDuplexMap {

    private final LinkDataMap sourceLinkDataMap;

    private final LinkDataMap targetLinkDataMap;

	public LinkDataDuplexMap() {
        this.sourceLinkDataMap = new LinkDataMap();
        this.targetLinkDataMap = new LinkDataMap();
	}

    public LinkDataDuplexMap(LinkDataMap sourceLinkDataMap) {
        if (sourceLinkDataMap == null) {
            throw new NullPointerException("sourceLinkDataMap must not be null");
        }
        this.sourceLinkDataMap = new LinkDataMap(sourceLinkDataMap);
        this.targetLinkDataMap = new LinkDataMap();
    }

    public LinkDataMap getSourceLinkDataMap() {
        return sourceLinkDataMap;
    }

    public Collection<LinkData> getSourceLinkDataList() {
        return sourceLinkDataMap.getLinkDataList();
    }

    public LinkDataMap getTargetLinkDataMap() {
        return targetLinkDataMap;
    }

    public Collection<LinkData> getTargetLinkDataList() {
        return targetLinkDataMap.getLinkDataList();
    }



    public void addLinkStatisticsDataSet(LinkDataDuplexMap linkDataDuplexMap) {
        if (linkDataDuplexMap == null) {
            throw new NullPointerException("linkDataDuplexMap must not be null");
        }
        for (LinkData copyLinkData : linkDataDuplexMap.sourceLinkDataMap.getLinkDataList()) {
            addSourceLinkData(copyLinkData);
        }
        for (LinkData copyLinkData : linkDataDuplexMap.targetLinkDataMap.getLinkDataList()) {
            addTargetLinkData(copyLinkData);
        }
    }

    public void addSourceLinkData(LinkData copyLinkData) {
        if (copyLinkData == null) {
            throw new NullPointerException("copyLinkData must not be null");
        }
        sourceLinkDataMap.addLinkData(copyLinkData);
    }


    public void addTargetLinkData(LinkData copyLinkData) {
        if (copyLinkData == null) {
            throw new NullPointerException("copyLinkData must not be null");
        }
        targetLinkDataMap.addLinkData(copyLinkData);
    }


    public int size() {
        return sourceLinkDataMap.size() + targetLinkDataMap.size();
    }


    public LinkData getSourceLinkData(LinkKey findLinkKey) {
        if (findLinkKey == null) {
            throw new NullPointerException("findLinkKey must not be null");
        }

        return sourceLinkDataMap.getLinkData(findLinkKey);
    }

    public LinkData getTargetLinkData(LinkKey findLinkKey) {
        if (findLinkKey == null) {
            throw new NullPointerException("findLinkKey must not be null");
        }

        return targetLinkDataMap.getLinkData(findLinkKey);
    }
}
