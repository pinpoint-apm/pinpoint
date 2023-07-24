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
package com.navercorp.pinpoint.web.realtime.activethread.count.websocket;

import com.navercorp.pinpoint.web.realtime.activethread.count.service.ActiveThreadCountService;
import com.navercorp.pinpoint.web.realtime.activethread.count.service.ActiveThreadCountWebServiceConfig;
import com.navercorp.pinpoint.web.realtime.service.RealtimeWebServiceConfig;
import com.navercorp.pinpoint.web.websocket.PinpointWebSocketHandler;
import com.navercorp.pinpoint.web.websocket.message.PinpointWebSocketMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({ ActiveThreadCountWebServiceConfig.class, RealtimeWebServiceConfig.class })
public class ActiveThreadCountWebSocketConfig {

    @Bean
    public PinpointWebSocketHandler redisActiveThreadCountHandler(PinpointWebSocketMessageConverter converter, ActiveThreadCountService atcSessionFactory) {
        return new ActiveThreadCountHandlerImpl(converter, atcSessionFactory);
    }

}
