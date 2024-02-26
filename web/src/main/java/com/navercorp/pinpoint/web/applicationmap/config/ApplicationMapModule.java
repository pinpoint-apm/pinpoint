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

package com.navercorp.pinpoint.web.applicationmap.config;

import com.navercorp.pinpoint.common.server.executor.ExecutorCustomizer;
import com.navercorp.pinpoint.common.server.executor.ExecutorProperties;
import com.navercorp.pinpoint.common.server.util.CallerUtils;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapBuilderFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramAppenderFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInfoAppenderFactory;
import com.navercorp.pinpoint.web.applicationmap.map.ApplicationsMapCreatorFactory;
import com.navercorp.pinpoint.web.applicationmap.map.LinkSelectorFactory;
import com.navercorp.pinpoint.web.applicationmap.map.processor.ApplicationLimiterProcessorFactory;
import com.navercorp.pinpoint.web.applicationmap.map.processor.LinkDataMapProcessor;
import com.navercorp.pinpoint.web.applicationmap.service.LinkDataMapService;
import com.navercorp.pinpoint.web.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.task.ChainedTaskDecorator;
import com.navercorp.pinpoint.web.task.RequestContextPropagatingTaskDecorator;
import com.navercorp.pinpoint.web.task.SecurityContextPropagatingTaskDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Configuration
@ComponentScan(basePackages = {
        "com.navercorp.pinpoint.web.applicationmap.service",
        "com.navercorp.pinpoint.web.applicationmap.controller"
})
@Import(MapHbaseConfiguration.class)
public class ApplicationMapModule {
    private static final Logger logger = LogManager.getLogger(ApplicationMapModule.class);

    public ApplicationMapModule() {
        logger.info("Install {}", ApplicationMapModule.class.getSimpleName());
    }

    @Bean
    public ApplicationMapBuilderFactory applicationMapBuilderFactory(
            NodeHistogramAppenderFactory nodeHistogramAppenderFactory,
            ServerInfoAppenderFactory serverInfoAppenderFactory) {
        return new ApplicationMapBuilderFactory(nodeHistogramAppenderFactory, serverInfoAppenderFactory);
    }

    @Bean
    public NodeHistogramAppenderFactory nodeHistogramAppenderFactory(@Qualifier("nodeHistogramAppendExecutor") Executor executor) {
        return new NodeHistogramAppenderFactory(executor);
    }

    @Bean
    public ServerInfoAppenderFactory serverInfoAppenderFactory(@Qualifier("serverInfoAppendExecutor") Executor executor) {
        return new ServerInfoAppenderFactory(executor);
    }

    @Bean
    public ApplicationsMapCreatorFactory applicationsMapCreatorFactory(@Qualifier("applicationsMapCreateExecutor") Executor executor) {
        return new ApplicationsMapCreatorFactory(executor);
    }

    @Bean
    public Supplier<LinkDataMapProcessor> applicationLimiterProcessorFactory(@Value("${pinpoint.server-map.read-limit:100}") int limit) {
        return new ApplicationLimiterProcessorFactory(limit);
    }

    @Bean
    public LinkSelectorFactory linkSelectorFactory(LinkDataMapService linkDataMapService,
                                                   ApplicationsMapCreatorFactory applicationsMapCreatorFactory,
                                                   HostApplicationMapDao hostApplicationMapDao,
                                                   Optional<ServerMapDataFilter> serverMapDataFilter,
                                                   Supplier<LinkDataMapProcessor> applicationLimiterProcessorFactory) {
        return new LinkSelectorFactory(linkDataMapService, applicationsMapCreatorFactory, hostApplicationMapDao, serverMapDataFilter, applicationLimiterProcessorFactory);
    }

    @Bean
    @Validated
    @ConfigurationProperties("web.servermap.creator.worker")
    public ExecutorProperties creatorExecutorProperties() {
        return new ExecutorProperties();
    }

    @Bean
    public Executor applicationsMapCreateExecutor(@Qualifier("creatorExecutorProperties") ExecutorProperties executorProperties) {
        ExecutorCustomizer<ThreadPoolTaskExecutor> customizer = executorCustomizer();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        customizer.customize(executor, executorProperties);

        String beanName = CallerUtils.getCallerMethodName();
        executor.setThreadNamePrefix(beanName);
        return executor;
    }

    @Bean
    @Validated
    @ConfigurationProperties("web.servermap.appender.worker")
    public ExecutorProperties appenderExecutorProperties() {
        return new ExecutorProperties();
    }

    @Bean
    public Executor nodeHistogramAppendExecutor(@Qualifier("appenderExecutorProperties") ExecutorProperties executorProperties) {
        ExecutorCustomizer<ThreadPoolTaskExecutor> customizer = executorCustomizer();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        customizer.customize(executor, executorProperties);

        String beanName = CallerUtils.getCallerMethodName();
        executor.setThreadNamePrefix(beanName);
        return executor;
    }

    @Bean
    public Executor serverInfoAppendExecutor(@Qualifier("appenderExecutorProperties") ExecutorProperties executorProperties) {
        ExecutorCustomizer<ThreadPoolTaskExecutor> customizer = executorCustomizer();

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        customizer.customize(executor, executorProperties);

        String beanName = CallerUtils.getCallerMethodName();
        executor.setThreadNamePrefix(beanName);
        return executor;
    }


    public TaskDecorator contextPropagatingTaskDecorator() {
        TaskDecorator requestDecorator = new RequestContextPropagatingTaskDecorator();
        TaskDecorator securityDecorator = new SecurityContextPropagatingTaskDecorator();
        return new ChainedTaskDecorator(List.of(requestDecorator, securityDecorator));
    }

    @Bean
    public ExecutorCustomizer<ThreadPoolTaskExecutor> executorCustomizer() {
        TaskDecorator taskDecorator = contextPropagatingTaskDecorator();
        return new TaskExecutorCustomizer(taskDecorator);
    }

}
