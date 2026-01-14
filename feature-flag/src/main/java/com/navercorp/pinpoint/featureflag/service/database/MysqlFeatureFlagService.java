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

package com.navercorp.pinpoint.featureflag.service.database;

import com.navercorp.pinpoint.featureflag.dao.FeatureFlagDao;
import com.navercorp.pinpoint.featureflag.dao.vo.FeatureFlagParameter;
import com.navercorp.pinpoint.featureflag.dao.vo.FeatureFlagResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MysqlFeatureFlagService implements DatabaseFeatureFlagService {
    private final FeatureFlagDao featureFlagDao;
    private final Map<String, List<FeatureFlagResult>> enabledFeatureMap = new ConcurrentHashMap<>();
    private final Map<String, List<FeatureFlagResult>> disabledFeatureMap = new ConcurrentHashMap<>();

    public MysqlFeatureFlagService(FeatureFlagDao featureFlagDao) {
        this.featureFlagDao = Objects.requireNonNull(featureFlagDao, "featureFlagDao");
    }

    public void register(String featureName, FeatureFlagParameter.FeatureRule ruleType) {
        Objects.requireNonNull(featureName, "featureName");
        Objects.requireNonNull(ruleType, "ruleType");

        List<FeatureFlagResult> result = featureFlagDao.selectFeatureFlag(featureName, ruleType);
        if (ruleType == FeatureFlagParameter.FeatureRule.DISABLED) {
            disabledFeatureMap.put(featureName, result);
        } else {
            enabledFeatureMap.put(featureName, result);
        }
    }

    @Scheduled(fixedDelayString = "${featureflag.refresh-interval-ms:60000}")
    public void refreshFeatureFlags() {
        refreshMap(enabledFeatureMap, FeatureFlagParameter.FeatureRule.ENABLED);
        refreshMap(disabledFeatureMap, FeatureFlagParameter.FeatureRule.DISABLED);
    }

    @Override
    public boolean isDisabled(String featureName, String serviceName, String applicationName) {
        List<FeatureFlagResult> list = disabledFeatureMap.get(featureName);
        if (list == null) {
            return false;
        }

        return list.stream().anyMatch(featureFlagResult ->
            featureFlagResult.matches(serviceName, applicationName)
        );
    }

    @Override
    public boolean isEnabled(String featureName, String serviceName, String applicationName) {
        List<FeatureFlagResult> list = enabledFeatureMap.get(featureName);
        if (list == null) {
            return false;
        }

        return list.stream().anyMatch(featureFlagResult ->
                featureFlagResult.matches(serviceName, applicationName)
        );
    }


    private void refreshMap(Map<String, List<FeatureFlagResult>> map, FeatureFlagParameter.FeatureRule rule) {
        map.keySet().forEach(featureName ->
                map.computeIfPresent(featureName, (key, value) ->
                        featureFlagDao.selectFeatureFlag(featureName, rule)
                )
        );
    }
}
