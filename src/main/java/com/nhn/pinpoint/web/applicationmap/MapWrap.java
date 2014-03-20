package com.nhn.pinpoint.web.applicationmap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author emeroad
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class MapWrap {
    private final ApplicationMap applicationMap;
    private Long lastFetchedTimestamp;

    public MapWrap(ApplicationMap applicationMap) {
        this.applicationMap = applicationMap;
    }


    public void setLastFetchedTimestamp(Long lastFetchedTimestamp) {
        this.lastFetchedTimestamp = lastFetchedTimestamp;
    }

    @JsonProperty("applicationMapData")
    public ApplicationMap getApplicationMap() {
        return applicationMap;
    }

    @JsonProperty("lastFetchedTimestamp")
    public Long getLastFetchedTimestamp() {
        return lastFetchedTimestamp;
    }
}
