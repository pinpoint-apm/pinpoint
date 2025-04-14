package com.navercorp.pinpoint.common.server.metric.dao;

import java.util.Objects;

public class FixedNameManager implements NameManager {

    private final String fixedName;

    public FixedNameManager(String fixedName) {
        this.fixedName = Objects.requireNonNull(fixedName, "fixedName");
    }

    public String getName(String key) {
        return fixedName;
    }
}
