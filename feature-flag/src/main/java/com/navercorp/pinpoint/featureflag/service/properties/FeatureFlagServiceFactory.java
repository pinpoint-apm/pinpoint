/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.featureflag.service.properties;

import com.navercorp.pinpoint.featureflag.config.FeatureFlagProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FeatureFlagServiceFactory {
    private static final Logger logger = LogManager.getLogger(FeatureFlagServiceFactory.class);

    private final Map<String, FeatureFlagProperties.FeatureSpec> featureMap;

    public FeatureFlagServiceFactory(FeatureFlagProperties properties) {
        this.featureMap = properties.stream()
                .collect(Collectors.toMap(FeatureFlagProperties.FeatureSpec::name, Function.identity(), (a, b) -> {
                    logger.warn("Duplicate feature spec found: {}", a.name());
                    return b;
                }));
        logSpecs(featureMap.values());
    }

    private static void logSpecs(Collection<FeatureFlagProperties.FeatureSpec> specList) {
        for (FeatureFlagProperties.FeatureSpec feature : specList) {
            logger.info(feature);
            if (feature.enabled() != null && (feature.enabledFor() != null || feature.disabledFor() != null)) {
                logger.warn("{}: enabled and application specific list is set at the same time", feature.name());
            }
            if (feature.enabledFor() != null && feature.disabledFor() != null) {
                logger.warn("{}: enabledFor and disabledFor are set at the same time", feature.name());
            }
        }
    }

    public FeatureFlagService get(String featureName) {
        FeatureFlagProperties.FeatureSpec feature = featureMap.get(featureName);
        if (feature == null) {
            return new EnabledFeatureFlagService();
        }

        final boolean enabled;
        if (feature.enabled() == null) {
            // if there is no default
            // enabled when disabledFor is set
            // disabled when enabledFor is set
            // enabled when both are set or both are not set
            enabled = feature.disabledFor() != null || feature.enabledFor() == null;
        } else {
            enabled = feature.enabled();
        }

        return new SimpleFeatureFlagService(enabled, feature.enabledFor(), feature.disabledFor());
    }
}
