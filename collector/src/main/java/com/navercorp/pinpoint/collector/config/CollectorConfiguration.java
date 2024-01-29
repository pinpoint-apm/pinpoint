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
 *
 */

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.collector.sampler.SimpleSpanSamplerFactory;
import com.navercorp.pinpoint.collector.sampler.SpanSamplerFactory;
import com.navercorp.pinpoint.common.server.executor.ExecutorCustomizer;
import com.navercorp.pinpoint.common.server.executor.ExecutorProperties;
import com.navercorp.pinpoint.common.server.executor.ThreadPoolExecutorCustomizer;
import com.navercorp.pinpoint.common.server.util.CallerUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.ExecutorService;

@Configuration
public class CollectorConfiguration {

    @Bean
    public ExecutorCustomizer<ThreadPoolExecutorFactoryBean> collectorExecutorCustomizer() {
        return new ThreadPoolExecutorCustomizer();
    }

    @Bean
    @Validated
    @ConfigurationProperties(prefix="collector.agent-event-worker")
    public ExecutorProperties agentEventWorkerExecutorProperties() {
        return new ExecutorProperties();
    }

    @Bean
    public FactoryBean<ExecutorService> agentEventWorker(@Qualifier("collectorExecutorCustomizer") ExecutorCustomizer<ThreadPoolExecutorFactoryBean> executorCustomizer,
                                                        @Qualifier("agentEventWorkerExecutorProperties") ExecutorProperties properties) {

        ThreadPoolExecutorFactoryBean factory = new ThreadPoolExecutorFactoryBean();
        executorCustomizer.customize(factory, properties);

        String beanName = CallerUtils.getCallerMethodName();
        factory.setThreadNamePrefix(beanName);
        return factory;
    }

    @Bean
    public SpanSamplerFactory spanSamplerFactory(CollectorProperties collectorProperties) {
        return new SimpleSpanSamplerFactory(collectorProperties);
    }

}
