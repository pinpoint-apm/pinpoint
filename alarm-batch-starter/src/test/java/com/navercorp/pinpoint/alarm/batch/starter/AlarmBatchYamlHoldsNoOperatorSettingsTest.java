/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.alarm.batch.starter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * That the packaged yaml holds nothing an operator is meant to set.
 *
 * <p>{@link AlarmBatchStarter} adds the file named by its configuration-location property
 * with {@code addLast}, which puts it <em>below</em> a packaged {@code application.yml} and
 * above a {@code @PropertySource}. A value that moves back into the yaml is therefore not
 * merely duplicated -- it becomes unchangeable by the one mechanism this deployable offers
 * for changing it, and the operator's file is read and then loses, silently.
 *
 * <p>Checking the keys rather than booting a context is deliberate: the rule being kept is
 * about where a setting is declared, and that is a property of the file.
 */
class AlarmBatchYamlHoldsNoOperatorSettingsTest {

    /** Everything an installation sets belongs in the properties files, not here. */
    private static final List<String> OPERATOR_PREFIXES =
            List.of("alarm.", "pinpoint.modules.batch.alarm.", "spring.datasource.",
                    "spring.meta-datasource.", "spring.pinot-datasource.", "hbase.");

    @Test
    void theYamlDeclaresNoSettingAnExternalFileWouldNeedToOverride() throws Exception {
        Properties yaml = load("application.yml");

        Set<String> offenders = yaml.stringPropertyNames().stream()
                .filter(key -> OPERATOR_PREFIXES.stream().anyMatch(key::startsWith))
                .collect(Collectors.toSet());

        assertTrue(offenders.isEmpty(),
                "these sit above the external configuration file and cannot be overridden by it, "
                        + "so they belong in alarm-batch-root.properties: " + offenders);
    }

    /** The banner names keys; it does not set them, so it is allowed to mention the prefixes. */
    @Test
    void theYamlStillCarriesWhatOnlyItCanCarry() throws Exception {
        Properties yaml = load("application.yml");

        assertTrue(yaml.containsKey("spring.profiles.active"),
                "the profile decides which properties file is read, so it cannot live in one");
        assertTrue(yaml.containsKey("pinpoint.banner.configs"),
                "the banner list is what the startup log prints, not a setting");
    }

    private static Properties load(String name) {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource(name));
        factory.afterPropertiesSet();
        return factory.getObject();
    }
}
