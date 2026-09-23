package com.navercorp.pinpoint.featureflag.config;

import com.navercorp.pinpoint.featureflag.service.properties.FeatureFlagTarget;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "features")
public class FeatureFlagProperties extends ArrayList<FeatureFlagProperties.FeatureSpec> {
    public record FeatureSpec(String name, Boolean enabled, List<FeatureFlagTarget> enabledFor, List<FeatureFlagTarget> disabledFor) {
    }
}
