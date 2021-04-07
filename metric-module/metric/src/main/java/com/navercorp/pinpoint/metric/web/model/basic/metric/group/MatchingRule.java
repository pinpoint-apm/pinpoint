package com.navercorp.pinpoint.metric.web.model.basic.metric.group;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
public enum MatchingRule {
    EXACT(1, "exact"),
    CONTAIN(2, "contain"),
    ALL(3, "all"),
    UNKNOWN(100, "unknown");

    private final int code;
    private final String value;

    MatchingRule(int code, String value) {
        this.code = code;
        this.value = Objects.requireNonNull(value, "value");
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static MatchingRule getByCode(int code) {
        for (MatchingRule matchingRule : MatchingRule.values()) {
            if (matchingRule.code == code) {
                return matchingRule;
            }
        }
        throw new IllegalArgumentException("Unknown code : " + code);
    }

    public static MatchingRule getByValue(String value) {
        for (MatchingRule matchingRule : MatchingRule.values()) {
            if (matchingRule.value.equalsIgnoreCase(value)) {
                return matchingRule;
            }
        }
        throw new IllegalArgumentException("Unknown value : " + value);
    }
}
