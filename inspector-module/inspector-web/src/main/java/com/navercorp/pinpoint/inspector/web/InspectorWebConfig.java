/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.web;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.navercorp.pinpoint.common.server.config.YamlConfiguration;
import com.navercorp.pinpoint.inspector.web.config.InspectorWebPinotDaoConfiguration;
import com.navercorp.pinpoint.inspector.web.definition.Mappings;
import com.navercorp.pinpoint.inspector.web.definition.YMLInspectorManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author minwoo.jung
 */
@Configuration
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.inspector.web.controller",
        "com.navercorp.pinpoint.inspector.web.dao",
        "com.navercorp.pinpoint.inspector.web.definition",
        "com.navercorp.pinpoint.inspector.web.service",
})
@Import({
        InspectorWebPinotDaoConfiguration.class,
        YamlConfiguration.class
})
@ConditionalOnProperty(name = "pinpoint.modules.web.inspector.enabled", havingValue = "true")
public class InspectorWebConfig {

    @Bean
    public Mappings agentInspectorDefinition(@Value(YMLInspectorManager.AGENT_DEFINITION_YML_PATH)
                                                      Resource inspectorMetric,
                                                      YAMLMapper mapper) throws IOException {

        InputStream stream = inspectorMetric.getInputStream();

        return mapper.readValue(stream, Mappings.class);
    }

    @Bean
    public Mappings applicationInspectorDefinition(@Value(YMLInspectorManager.APPLICATION_DEFINITION_YML_PATH)
                                             Resource inspectorMetric,
                                             YAMLMapper mapper) throws IOException {

        InputStream stream = inspectorMetric.getInputStream();

        return mapper.readValue(stream, Mappings.class);
    }

}
