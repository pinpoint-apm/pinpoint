/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.plugin.activemq.client;

import com.navercorp.pinpoint.bootstrap.config.ExcludePathFilter;
import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.SkipFilter;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author HyunGil Jeong
 */
public class ActiveMQClientPluginConfig {

    public static final String DEFAULT_DESTINATION_PATH_SEPARATOR = ".";

    private final boolean traceActiveMQClient;
    private final boolean traceActiveMQClientProducer;
    private final boolean traceActiveMQClientConsumer;
    private final boolean traceActiveMQTextMessage;
    private final Filter<String> excludeDestinationFilter;

    public ActiveMQClientPluginConfig(ProfilerConfig config) {
        this.traceActiveMQClient = config.readBoolean("profiler.activemq.client.enable", true);
        this.traceActiveMQClientProducer = config.readBoolean("profiler.activemq.client.producer.enable", true);
        this.traceActiveMQClientConsumer = config.readBoolean("profiler.activemq.client.consumer.enable", true);
        this.traceActiveMQTextMessage = config.readBoolean("profiler.activemq.client.trace.message", false);
        String excludeDestinationPathSeparator = config.readString("profiler.activemq.client.destination.separator", DEFAULT_DESTINATION_PATH_SEPARATOR);
        if (StringUtils.isEmpty(excludeDestinationPathSeparator)) {
            excludeDestinationPathSeparator = DEFAULT_DESTINATION_PATH_SEPARATOR;
        }
        String excludeDestinations = config.readString("profiler.activemq.client.destination.exclude", "");
        if (!excludeDestinations.isEmpty()) {
            this.excludeDestinationFilter = new ExcludePathFilter(excludeDestinations, excludeDestinationPathSeparator);
        } else {
            this.excludeDestinationFilter = new SkipFilter<String>();
        }
    }

    public boolean isTraceActiveMQClient() {
        return this.traceActiveMQClient;
    }

    public boolean isTraceActiveMQClientProducer() {
        return this.traceActiveMQClientProducer;
    }

    public boolean isTraceActiveMQClientConsumer() {
        return this.traceActiveMQClientConsumer;
    }

    public boolean isTraceActiveMQTextMessage() {
        return traceActiveMQTextMessage;
    }

    public Filter<String> getExcludeDestinationFilter() {
        return this.excludeDestinationFilter;
    }

    @Override
    public String toString() {
        return "ActiveMQClientPluginConfig{" +
                "traceActiveMQClient=" + traceActiveMQClient +
                ", traceActiveMQClientProducer=" + traceActiveMQClientProducer +
                ", traceActiveMQClientConsumer=" + traceActiveMQClientConsumer +
                ", traceActiveMQTextMessage=" + traceActiveMQTextMessage +
                ", excludeDestinationFilter=" + excludeDestinationFilter +
                '}';
    }
}