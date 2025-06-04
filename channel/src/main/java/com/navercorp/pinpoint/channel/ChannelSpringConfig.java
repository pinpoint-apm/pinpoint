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
package com.navercorp.pinpoint.channel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.channel.serde.JacksonSerdeFactory;
import com.navercorp.pinpoint.channel.serde.JsonSerdeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
public class ChannelSpringConfig {


    @Bean
    public ChannelProviderRepository channelProviderRepository(List<ChannelProviderRegistry> registries) {
        return new ChannelProviderRepository(registries);
    }

    @Bean
    public JsonSerdeFactory jsonSerdeFactory(ObjectMapper objectMapper) {
        return new JacksonSerdeFactory(objectMapper);
    }
}
