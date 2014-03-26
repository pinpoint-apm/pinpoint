package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.vo.Application;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class LinkStatisticsDataSet {

    private final LinkStatisticsData source;

    private final LinkStatisticsData target;

	public LinkStatisticsDataSet() {
        this.source = new LinkStatisticsData();
        this.target = new LinkStatisticsData();
	}

    public LinkStatisticsDataSet(LinkStatisticsData source) {
        this.source = source;
        this.target = new LinkStatisticsData();
    }

    public Collection<LinkStatistics> getSourceLinkStatData() {
        return source.getLinkStatData();
    }

    public LinkStatisticsData getSourceLinkData() {
        return source;
    }

    public LinkStatisticsData getTargetLinkData() {
        return target;
    }

    public Collection<LinkStatistics> getTargetLinkStatData() {
        return target.getLinkStatData();
    }



    public void addLinkStatisticsDataSet(LinkStatisticsDataSet linkStatisticsDataSet) {
        if (linkStatisticsDataSet == null) {
            throw new NullPointerException("linkStatisticsDataSet must not be null");
        }
        for (LinkStatistics copyLinkStatistics : linkStatisticsDataSet.source.getLinkStatData()) {
            addSourceLinkStatistics(copyLinkStatistics);
        }
        for (LinkStatistics copyLinkStatistics : linkStatisticsDataSet.target.getLinkStatData()) {
            addTargetLinkStatistics(copyLinkStatistics);
        }
    }

    public void addSourceLinkStatistics(LinkStatistics copyLinkStatistics) {
        if (copyLinkStatistics == null) {
            throw new NullPointerException("copyLinkStatistics must not be null");
        }
        source.addLinkStatistics(copyLinkStatistics);
    }


    public void addTargetLinkStatistics(LinkStatistics copyLinkStatistics) {
        if (copyLinkStatistics == null) {
            throw new NullPointerException("copyLinkStatistics must not be null");
        }
        target.addLinkStatistics(copyLinkStatistics);
    }


    public int size() {
        return source.size() + target.size();
    }

    public Map<Application, Set<AgentInfoBo>> getSourceAgentMap() {

        return this.source.getAgentMap();
    }


}
