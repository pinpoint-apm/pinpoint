package com.nhn.pinpoint.web.applicationmap.rawdata;

import java.util.Collection;

public class LinkDataDuplexMap {

    private final LinkDataMap source;

    private final LinkDataMap target;

	public LinkDataDuplexMap() {
        this.source = new LinkDataMap();
        this.target = new LinkDataMap();
	}

    public LinkDataDuplexMap(LinkDataMap source) {
        if (source == null) {
            throw new NullPointerException("source must not be null");
        }
        this.source = source;
        this.target = new LinkDataMap();
    }

    public LinkDataMap getSourceLinkData() {
        return source;
    }

    public Collection<LinkData> getSourceLinkStatData() {
        return source.getLinkStatData();
    }

    public LinkDataMap getTargetLinkData() {
        return target;
    }

    public Collection<LinkData> getTargetLinkStatData() {
        return target.getLinkStatData();
    }



    public void addLinkStatisticsDataSet(LinkDataDuplexMap linkDataDuplexMap) {
        if (linkDataDuplexMap == null) {
            throw new NullPointerException("linkDataDuplexMap must not be null");
        }
        for (LinkData copyLinkData : linkDataDuplexMap.source.getLinkStatData()) {
            addSourceLinkData(copyLinkData);
        }
        for (LinkData copyLinkData : linkDataDuplexMap.target.getLinkStatData()) {
            addTargetLinkData(copyLinkData);
        }
    }

    public void addSourceLinkData(LinkData copyLinkData) {
        if (copyLinkData == null) {
            throw new NullPointerException("copyLinkData must not be null");
        }
        source.addLinkStatistics(copyLinkData);
    }


    public void addTargetLinkData(LinkData copyLinkData) {
        if (copyLinkData == null) {
            throw new NullPointerException("copyLinkData must not be null");
        }
        target.addLinkStatistics(copyLinkData);
    }


    public int size() {
        return source.size() + target.size();
    }


}
