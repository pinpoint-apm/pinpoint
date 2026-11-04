/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server;

import tools.jackson.datatype.eclipsecollections.EclipseCollectionsModule;
import com.navercorp.pinpoint.common.server.uid.ServiceHeaderEnabled;
import com.navercorp.pinpoint.common.server.util.AgentEventMessageDeserializerV1;
import com.navercorp.pinpoint.common.server.util.AgentEventMessageSerializerV1;
import com.navercorp.pinpoint.common.timeseries.window.DefaultTimeSlot;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
public class CommonsServerConfiguration {

    // thrift--------------
    @Bean
    public AgentEventMessageDeserializerV1 agentEventMessageDeserializerV1() {
        return new AgentEventMessageDeserializerV1();
    }

    @Bean
    public AgentEventMessageSerializerV1 agentEventMessageSerializerV1() {
        return new AgentEventMessageSerializerV1();
    }


    @Bean
    public TimeSlot timeSlot() {
        return new DefaultTimeSlot();
    }

    @Bean
    public tools.jackson.databind.JacksonModule eclipseCollectionsModule() {
        return new EclipseCollectionsModule();
    }

    /**
     * Runs before Spring Boot's property-driven customizer, so {@code spring.jackson.*} can still override.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public JsonMapperBuilderCustomizer jackson2CompatibleCustomizer() {
        return Jackson::configureJackson2Compatible;
    }

    @Bean
    public ServiceHeaderEnabled serviceHeaderEnabled(@Value("${" + ServiceHeaderEnabled.ENABLED + ":true}") boolean enabled) {
        return new ServiceHeaderEnabled(enabled);
    }

}
