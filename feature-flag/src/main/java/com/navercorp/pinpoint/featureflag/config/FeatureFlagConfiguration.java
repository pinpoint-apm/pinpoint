package com.navercorp.pinpoint.featureflag.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.navercorp.pinpoint.featureflag.service.properties")
@EnableConfigurationProperties(FeatureFlagProperties.class)
public class FeatureFlagConfiguration {
}
