package com.navercorp.pinpoint.featureflag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;

@ConfigurationProperties(prefix = "features")
public class FeatureFlagProperties extends HashMap<String, FeatureFlagProperties.Feature> {
    public record Feature(Boolean enabled, List<String> enabledFor, List<String> disabledFor) {
    }
}
