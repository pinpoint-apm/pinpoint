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

package com.navercorp.pinpoint.common.server.executor;

import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

public class ThreadPoolExecutorCustomizer implements ExecutorCustomizer<ThreadPoolExecutorFactoryBean> {

    public void customize(ThreadPoolExecutorFactoryBean factoryBean, ExecutorProperties properties) {
        factoryBean.setCorePoolSize(properties.getCorePoolSize());
        factoryBean.setMaxPoolSize(properties.getMaxPoolSize());
        factoryBean.setQueueCapacity(properties.getQueueCapacity());

        factoryBean.setPrestartAllCoreThreads(properties.isPrestartAllCoreThreads());
        if (properties.getThreadNamePrefix() != null) {
            factoryBean.setThreadNamePrefix(properties.getThreadNamePrefix());
        }
        factoryBean.setPrestartAllCoreThreads(properties.isPrestartAllCoreThreads());
        factoryBean.setDaemon(properties.isDaemon());

        factoryBean.setKeepAliveSeconds(properties.getKeepAliveSeconds());
        factoryBean.setWaitForTasksToCompleteOnShutdown(properties.isWaitForTasksToCompleteOnShutdown());
        factoryBean.setAwaitTerminationSeconds(properties.getAwaitTerminationSeconds());
    }
}
