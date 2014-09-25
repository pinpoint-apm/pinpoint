package com.nhn.pinpoint.web.applicationmap;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nhn.pinpoint.web.view.FilterMapWrapSerializer;
import com.nhn.pinpoint.web.vo.scatter.ApplicationScatterScanResult;

import java.util.List;

/**
 * @author emeroad
 */
@JsonSerialize(using = FilterMapWrapSerializer.class)
public class FilterMapWrap {
    private final ApplicationMap applicationMap;
    private Long lastFetchedTimestamp;

    public FilterMapWrap(ApplicationMap applicationMap) {
        this.applicationMap = applicationMap;
    }


    public void setLastFetchedTimestamp(Long lastFetchedTimestamp) {
        this.lastFetchedTimestamp = lastFetchedTimestamp;
    }

    public ApplicationMap getApplicationMap() {
        return applicationMap;
    }

    public Long getLastFetchedTimestamp() {
        return lastFetchedTimestamp;
    }

    public List<ApplicationScatterScanResult> getApplicationScatterScanResult() {
        return this.applicationMap.getApplicationScatterScanResultList();
    }
}
