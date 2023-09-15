/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.common.server.trace.ApiParserProvider;
import com.navercorp.pinpoint.common.server.util.AgentEventMessageDeserializer;
import com.navercorp.pinpoint.thrift.io.AgentEventHeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.CommandHeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.CommandHeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * @author youngjin.kim2
 */
@Configuration
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.web.service",
})
@Import({
        HyperLinkConfiguration.class,
        WebServiceConfig.CommonConfig.class,
})
public class WebServiceConfig {


    @Configuration
    public static class CommonConfig {

        @Bean
        public ApiParserProvider apiParserProvider() {
            return new ApiParserProvider();
        }

        @Bean
        public SerializerFactory<HeaderTBaseSerializer> commandHeaderTBaseSerializerFactory() {
            return CommandHeaderTBaseSerializerFactory.getDefaultInstance();
        }

        @Bean
        public DeserializerFactory<HeaderTBaseDeserializer> commandHeaderTBaseDeserializerFactory() {
            return CommandHeaderTBaseDeserializerFactory.getDefaultInstance();
        }

        @Bean
        public AgentEventMessageDeserializer agentEventMessageDeserializer() {
            DeserializerFactory<HeaderTBaseDeserializer> factory1 = commandHeaderTBaseDeserializerFactory();
            DeserializerFactory<HeaderTBaseDeserializer> factory2 = new AgentEventHeaderTBaseDeserializerFactory();
            return new AgentEventMessageDeserializer(List.of(factory1, factory2));
        }
    }
}
