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
package com.navercorp.pinpoint.web.realtime.activethread.count;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.web.realtime.activethread.count.service.ActiveThreadCountService;
import com.navercorp.pinpoint.web.realtime.activethread.count.service.ActiveThreadCountWebServiceConfig;
import com.navercorp.pinpoint.web.realtime.activethread.count.websocket.RedisActiveThreadCountWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
@Import(ActiveThreadCountWebServiceConfig.class)
public class WebActiveThreadCountConfig {

    @Bean
    RedisActiveThreadCountWebSocketHandler redisActiveThreadCountWebSocketHandler(
            ActiveThreadCountService service,
            ObjectMapper objectMapper
    ) {
        return new RedisActiveThreadCountWebSocketHandler(service, objectMapper);
    }

}
