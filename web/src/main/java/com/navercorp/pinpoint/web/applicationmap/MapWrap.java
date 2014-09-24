package com.nhn.pinpoint.web.applicationmap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author emeroad
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class MapWrap {
    private final ApplicationMap applicationMap;

    public MapWrap(ApplicationMap applicationMap) {
        this.applicationMap = applicationMap;
    }

    @JsonProperty("applicationMapData")
    public ApplicationMap getApplicationMap() {
        return applicationMap;
    }

}
