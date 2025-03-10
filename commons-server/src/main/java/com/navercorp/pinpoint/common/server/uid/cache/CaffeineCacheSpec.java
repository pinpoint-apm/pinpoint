package com.navercorp.pinpoint.common.server.uid.cache;

import java.util.Map;
import java.util.stream.Collectors;

public class CaffeineCacheSpec {

    private Map<String, String> spec;

    public Map<String, String> getSpec() {
        return spec;
    }

    public void setSpec(Map<String, String> spec) {
        this.spec = spec;
    }

    public String getSpecification() {
        return spec.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(","));
    }
}
