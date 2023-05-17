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
package com.navercorp.pinpoint.web.realtime.activethread.dump;

import com.navercorp.pinpoint.pubsub.endpoint.PubSubClientFactory;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubMonoClient;
import com.navercorp.pinpoint.realtime.RealtimePubSubServiceDescriptors;
import com.navercorp.pinpoint.realtime.dto.ATDDemand;
import com.navercorp.pinpoint.realtime.dto.ATDSupply;
import com.navercorp.pinpoint.redis.pubsub.RedisPubSubConfig;
import com.navercorp.pinpoint.web.realtime.RealtimeWebCommonConfig;
import com.navercorp.pinpoint.web.service.ActiveThreadDumpService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({ RealtimeWebCommonConfig.class, RedisPubSubConfig.class })
public class WebActiveThreadDumpConfig {

    @Bean
    PubSubMonoClient<ATDDemand, ATDSupply> atdEndpoint(PubSubClientFactory clientFactory) {
        return clientFactory.build(RealtimePubSubServiceDescriptors.ATD);
    }

    @Bean
    ActiveThreadDumpService activeThreadDumpService(PubSubMonoClient<ATDDemand, ATDSupply> endpoint) {
        return new ActiveThreadDumpServiceImpl(endpoint);
    }

}
