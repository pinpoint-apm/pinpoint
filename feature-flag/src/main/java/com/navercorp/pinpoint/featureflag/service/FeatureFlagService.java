package com.navercorp.pinpoint.featureflag.service;

public interface FeatureFlagService {
    boolean isEnabled(String featureName, String applicationName);

    default boolean isDisabled(String featureName, String applicationName) {
        return !isEnabled(featureName, applicationName);
    }
}
