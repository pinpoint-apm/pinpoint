package com.nhn.pinpoint.web.applicationmap;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

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
