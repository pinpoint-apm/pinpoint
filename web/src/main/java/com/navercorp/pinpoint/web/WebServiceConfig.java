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

import com.navercorp.pinpoint.web.service.ActiveThreadDumpService;
import com.navercorp.pinpoint.web.service.ActiveThreadDumpServiceImpl;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.service.EchoService;
import com.navercorp.pinpoint.web.service.EchoServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author youngjin.kim2
 */
@Configuration
@ComponentScan(basePackages = { "com.navercorp.pinpoint.web.service" })
public class WebServiceConfig {

    @Bean
    @ConditionalOnMissingBean(ActiveThreadDumpService.class)
    ActiveThreadDumpService activeThreadDumpService(AgentService agentService) {
        return new ActiveThreadDumpServiceImpl(agentService);
    }

    @Bean
    @ConditionalOnMissingBean(EchoService.class)
    EchoService echoService(AgentService agentService) {
        return new EchoServiceImpl(agentService);
    }

}
