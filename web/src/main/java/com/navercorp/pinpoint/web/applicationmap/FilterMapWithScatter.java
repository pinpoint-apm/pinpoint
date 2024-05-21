package com.navercorp.pinpoint.web.applicationmap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Map;
import java.util.Objects;

public class FilterMapWithScatter {
    private final ApplicationMap applicationMap;
    private final Map<Application, ScatterData> applicationScatterDataMap;

    public FilterMapWithScatter(ApplicationMap applicationMap, Map<Application, ScatterData> applicationScatterDataMap) {
        this.applicationMap = Objects.requireNonNull(applicationMap, "applicationMap");
        this.applicationScatterDataMap = Objects.requireNonNull(applicationScatterDataMap, "applicationScatterDataMap");
    }

    @JsonValue
    public ApplicationMap getApplicationMap() {
        return applicationMap;
    }

    @JsonIgnore
    public Map<Application, ScatterData> getScatterDataMap() {
        return applicationScatterDataMap;
    }
}
