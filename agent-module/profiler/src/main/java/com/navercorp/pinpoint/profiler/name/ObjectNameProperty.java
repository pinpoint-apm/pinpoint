package com.navercorp.pinpoint.profiler.name;

import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Objects;

public class ObjectNameProperty {
    private final String key;
    private final String value;

    private final IdSourceType sourceType;
    private final AgentIdType idType;

    public ObjectNameProperty(String key, String value, IdSourceType sourceType, AgentIdType idType) {
        this.key = Objects.requireNonNull(key, "key");
        this.value = value;
        this.sourceType = sourceType;
        this.idType = idType;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public IdSourceType getSourceType() {
        return sourceType;
    }

    public AgentIdType getIdType() {
        return idType;
    }

    @Override
    public String toString() {
        return key + "=" + value + "(" + sourceType + ", " + idType + ")";
    }

    public boolean hasLengthValue() {
        return StringUtils.hasLength(value);
    }

    public static boolean hasLengthValue(ObjectNameProperty property) {
        if (property == null) {
            return false;
        }
        if (property.hasLengthValue()) {
            return true;
        }
        return false;
    }
}
