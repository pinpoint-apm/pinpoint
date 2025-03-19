package com.navercorp.pinpoint.featureflag.service;

public interface FeatureFlagService {
    boolean isEnabled(String applicationName);

    default boolean isDisabled(String applicationName) {
        return !isEnabled(applicationName);
    }
}
