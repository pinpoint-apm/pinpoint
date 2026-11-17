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
 */

package com.navercorp.pinpoint.common.server.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import static org.assertj.core.api.Assertions.assertThat;

class YamlConfigurationTest {

    /**
     * The YAMLMapper bean is also an ObjectMapper.
     * It must neither replace the auto-configured JsonMapper nor make the ObjectMapper injection ambiguous.
     */
    @Test
    void yamlMapper_doesNotReplaceJsonMapper() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(YamlConfiguration.class, JacksonAutoConfiguration.class)) {
            YAMLMapper yamlMapper = context.getBean(YAMLMapper.class);
            JsonMapper jsonMapper = context.getBean(JsonMapper.class);

            assertThat(context.getBeansOfType(ObjectMapper.class)).hasSize(2);
            // resolved by @Primary of the auto-configured JsonMapper
            assertThat(context.getBean(ObjectMapper.class))
                    .isSameAs(jsonMapper)
                    .isNotSameAs(yamlMapper);
        }
    }

    @Test
    void yamlMapper_readsYaml() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(YamlConfiguration.class)) {
            YAMLMapper yamlMapper = context.getBean(YAMLMapper.class);

            Name name = yamlMapper.readValue("name: pinpoint\n", Name.class);

            assertThat(name.name()).isEqualTo("pinpoint");
        }
    }

    record Name(String name) {
    }
}
