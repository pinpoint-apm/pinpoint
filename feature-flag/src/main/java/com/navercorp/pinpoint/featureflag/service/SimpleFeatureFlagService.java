package com.navercorp.pinpoint.featureflag.service;

import org.springframework.lang.Nullable;

import java.util.List;

public class SimpleFeatureFlagService implements FeatureFlagService {
    private final boolean defaultFlag;

    private final List<String> enabledApplications;
    private final List<String> disabledApplications;

    public SimpleFeatureFlagService(boolean defaultFlag, @Nullable List<String> enabledApplications, @Nullable List<String> disabledApplications) {
        this.defaultFlag = defaultFlag;
        this.enabledApplications = enabledApplications;
        this.disabledApplications = disabledApplications;
    }

    @Override
    public boolean isEnabled(String applicationName) {
        // disabledApplications has higher priority
        if (disabledApplications != null && disabledApplications.contains(applicationName)) {
            return false;
        }
        if (enabledApplications != null && enabledApplications.contains(applicationName)) {
            return true;
        }
        return defaultFlag;
    }
}
