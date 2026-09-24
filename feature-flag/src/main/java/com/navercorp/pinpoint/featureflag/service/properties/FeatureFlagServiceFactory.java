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
import com.navercorp.pinpoint.featureflag.config.FeatureFlagProperties.FeatureSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FeatureFlagServiceFactory {
    private static final Logger logger = LogManager.getLogger(FeatureFlagServiceFactory.class);

    private final Map<String, FeatureSpec> featureMap;

    public FeatureFlagServiceFactory(FeatureFlagProperties properties) {
        this.featureMap = properties.stream()
                .map(FeatureFlagServiceFactory::validSpec)
                .collect(Collectors.toMap(FeatureSpec::name, Function.identity(), (a, b) -> {
                    logger.warn("Duplicate feature spec found: {}", a.name());
                    return b;
                }));
        logSpecs(featureMap.values());
    }

    private static FeatureSpec validSpec(FeatureSpec spec) {
        return new FeatureSpec(spec.name(), spec.enabled(),
                validTargets(spec.name(), "enabled-for", spec.enabledFor()),
                validTargets(spec.name(), "disabled-for", spec.disabledFor()));
    }

    private static List<FeatureFlagTarget> validTargets(String name, String property, List<FeatureFlagTarget> targets) {
        if (targets == null) {
            return null;
        }
        List<FeatureFlagTarget> valid = new ArrayList<>();
        for (FeatureFlagTarget target : targets) {
            if (target == null || !StringUtils.hasText(target.serviceName()) || !StringUtils.hasText(target.applicationName())) {
                logger.warn("Ignoring invalid feature flag target in {} for feature {}: {}", property, name, target);
            } else {
                valid.add(target);
            }
        }
        return valid.isEmpty() ? null : List.copyOf(valid);
    }

    private static void logSpecs(Collection<FeatureSpec> specList) {
        for (FeatureSpec feature : specList) {
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
        FeatureSpec feature = featureMap.get(featureName);
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
