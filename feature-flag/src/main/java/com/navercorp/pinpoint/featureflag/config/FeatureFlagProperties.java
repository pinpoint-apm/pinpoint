package com.navercorp.pinpoint.featureflag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "features")
public class FeatureFlagProperties extends ArrayList<FeatureFlagProperties.FeatureSpec> {
    public record FeatureSpec(String name, Boolean enabled, List<String> enabledFor, List<String> disabledFor) {
    }
}
