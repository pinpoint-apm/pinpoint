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
package com.navercorp.pinpoint.web.realtime.echo;

import com.navercorp.pinpoint.pubsub.endpoint.PubSubClientFactory;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubMonoClient;
import com.navercorp.pinpoint.realtime.RealtimePubSubServiceDescriptors;
import com.navercorp.pinpoint.realtime.dto.Echo;
import com.navercorp.pinpoint.redis.pubsub.RedisPubSubConfig;
import com.navercorp.pinpoint.web.realtime.RealtimeWebCommonConfig;
import com.navercorp.pinpoint.web.service.EchoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({ RealtimeWebCommonConfig.class, RedisPubSubConfig.class })
public class WebEchoConfig {

    @Bean
    PubSubMonoClient<Echo, Echo> echoEndpoint(PubSubClientFactory clientFactory) {
        return clientFactory.build(RealtimePubSubServiceDescriptors.ECHO);
    }

    @Bean
    EchoService echoService(PubSubMonoClient<Echo, Echo> echoEndpoint) {
        return new EchoServiceImpl(echoEndpoint);
    }

}
