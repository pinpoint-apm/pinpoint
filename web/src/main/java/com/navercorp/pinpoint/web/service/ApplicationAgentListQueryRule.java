package com.navercorp.pinpoint.web.service;

public enum ApplicationAgentListQueryRule {

    ALL,
    ACTIVE_STATUS,
    ACTIVE_RESPONSE;

    public static ApplicationAgentListQueryRule getByValue(String value, ApplicationAgentListQueryRule defaultValue) {
        try {
            return getByValue(value);
        } catch (IllegalArgumentException ignore) {
        }
        return defaultValue;
    }

    public static ApplicationAgentListQueryRule getByValue(String value) {
        for (ApplicationAgentListQueryRule applicationAgentListQueryRule : ApplicationAgentListQueryRule.values()) {
            if (applicationAgentListQueryRule.name().equalsIgnoreCase(value)) {
                return applicationAgentListQueryRule;
            }
        }
        throw new IllegalArgumentException("Unknown value : " + value);
    }
}
