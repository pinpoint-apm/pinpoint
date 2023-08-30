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
package com.navercorp.pinpoint.log.web.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.channel.serde.JacksonSerde;
import com.navercorp.pinpoint.log.web.service.LiveTailService;
import com.navercorp.pinpoint.log.web.service.LogServiceConfig;
import com.navercorp.pinpoint.log.web.vo.LiveTailBatch;
import com.navercorp.pinpoint.web.websocket.PinpointWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({ LogServiceConfig.class })
public class LogWebSocketConfig {

    @Bean
    PinpointWebSocketHandler logWebSocketHandler(
            LiveTailService liveTailService,
            ObjectMapper objectMapper
    ) {
        return new LogWebSocketHandler(
                liveTailService,
                JacksonSerde.byParameterized(objectMapper, List.class, LiveTailBatch.class)
        );
    }

}
