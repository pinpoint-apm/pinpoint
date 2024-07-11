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

package com.navercorp.pinpoint.collector.grpc;

import com.codahale.metrics.MetricRegistry;
import com.navercorp.pinpoint.collector.grpc.config.GrpcAgentConfiguration;
import com.navercorp.pinpoint.collector.grpc.config.GrpcAgentReceiverConfiguration;
import com.navercorp.pinpoint.collector.grpc.config.GrpcComponentConfiguration;
import com.navercorp.pinpoint.collector.grpc.config.GrpcKeepAliveScheduler;
import com.navercorp.pinpoint.collector.grpc.config.GrpcSpanConfiguration;
import com.navercorp.pinpoint.collector.grpc.config.GrpcSpanReceiverConfiguration;
import com.navercorp.pinpoint.collector.grpc.config.GrpcStatConfiguration;
import com.navercorp.pinpoint.collector.grpc.config.GrpcStatReceiverConfiguration;
import com.navercorp.pinpoint.collector.monitor.MonitoringExecutors;
import com.navercorp.pinpoint.common.server.executor.ExecutorCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

@Configuration
@Import({
        GrpcComponentConfiguration.class,

        GrpcAgentReceiverConfiguration.class,
        GrpcAgentConfiguration.class,

        GrpcSpanConfiguration.class,
        GrpcSpanReceiverConfiguration.class,

        GrpcStatConfiguration.class,
        GrpcStatReceiverConfiguration.class,

        GrpcKeepAliveScheduler.class
})
@ComponentScan({
        "com.navercorp.pinpoint.collector.receiver.grpc"
})
public class CollectorGrpcConfiguration {

    @Bean
    public MonitoringExecutors monitoringExecutors(
            @Qualifier("collectorExecutorCustomizer") ExecutorCustomizer<ThreadPoolExecutorFactoryBean> customizer,
            @Autowired(required = false) MetricRegistry metricRegistry) {
        return new MonitoringExecutors(customizer, metricRegistry);
    }

}
