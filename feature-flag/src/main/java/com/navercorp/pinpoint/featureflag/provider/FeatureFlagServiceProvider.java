package com.navercorp.pinpoint.featureflag.provider;

import com.navercorp.pinpoint.featureflag.service.FeatureFlagService;

public interface FeatureFlagServiceProvider {
    FeatureFlagService get(String featureName);
}
