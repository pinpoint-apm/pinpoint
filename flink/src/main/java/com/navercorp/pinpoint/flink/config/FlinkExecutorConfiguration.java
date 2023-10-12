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

package com.navercorp.pinpoint.flink.config;

import com.codahale.metrics.MetricRegistry;
import com.navercorp.pinpoint.collector.monitor.MonitoringExecutors;
import com.navercorp.pinpoint.common.server.executor.ExecutorCustomizer;
import com.navercorp.pinpoint.common.server.executor.ThreadPoolExecutorCustomizer;
import com.navercorp.pinpoint.common.server.thread.MonitoringExecutorProperties;
import com.navercorp.pinpoint.common.server.util.CallerUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class FlinkExecutorConfiguration {

    @Bean
    public ExecutorCustomizer<ThreadPoolExecutorFactoryBean> flinkExecutorCustomizer() {
        return new ThreadPoolExecutorCustomizer();
    }

    @Bean
    public MonitoringExecutors flnkMonitoringExecutors(@Autowired(required = false) MetricRegistry metricRegistry) {
        ExecutorCustomizer<ThreadPoolExecutorFactoryBean> customizer = flinkExecutorCustomizer();
        return new MonitoringExecutors(customizer, metricRegistry);
    }


    @Bean
    @Validated
    @ConfigurationProperties("flink.receiver.base.worker")
    public MonitoringExecutorProperties flinkWorkerExecutorProperties() {
        return new MonitoringExecutorProperties();
    }

    @Bean
    public FactoryBean<ExecutorService> flinkWorker(MonitoringExecutors executors) {
        String beanName = CallerUtils.getCallerMethodName();
        MonitoringExecutorProperties properties = flinkWorkerExecutorProperties();
        properties.setLogRate(1);
        ThreadPoolExecutorFactoryBean factoryBean = executors.newExecutorFactoryBean(properties, beanName);
        factoryBean.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        return factoryBean;
    }
}
