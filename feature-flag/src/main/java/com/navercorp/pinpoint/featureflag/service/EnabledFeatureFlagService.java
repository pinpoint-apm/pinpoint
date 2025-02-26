package com.navercorp.pinpoint.featureflag.service;

public class EnabledFeatureFlagService implements FeatureFlagService {
    @Override
    public boolean isEnabled(String applicationName) {
        return true;
    }
}
