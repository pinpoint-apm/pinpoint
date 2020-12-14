/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.rocketmq;

import java.util.List;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author messi-gao
 */
public class RocketMQConfig {

    static final String PRODUCER_ENABLE = "profiler.rocketmq.producer.enable";

    static final String CONSUMER_ENABLE = "profiler.rocketmq.consumer.enable";
    static final String CONSUMER_ENTRY_POINT = "profiler.rocketmq.consumer.entryPoint";
    static final String BASE_PACKAGE = "profiler.rocketmq.basePackage";

    private final boolean producerEnable;
    private final boolean consumerEnable;
    private final String rocketmqEntryPoint;
    private final List<String> basePackages;

    public RocketMQConfig(ProfilerConfig config) {
        /*
         * rocketmq
         */
        producerEnable = config.readBoolean(PRODUCER_ENABLE, false);
        consumerEnable = config.readBoolean(CONSUMER_ENABLE, false);
        rocketmqEntryPoint = config.readString(CONSUMER_ENTRY_POINT, "");
        basePackages = config.readList(BASE_PACKAGE);
    }

    public boolean isProducerEnable() {
        return producerEnable;
    }

    public boolean isConsumerEnable() {
        return consumerEnable;
    }

    public String getRocketmqEntryPoint() {
        return rocketmqEntryPoint;
    }

    public List<String> getBasePackages() {
        return basePackages;
    }

    @Override
    public String toString() {
        return "RocketMQConfig{" +
               "producerEnable=" + producerEnable +
               ", consumerEnable=" + consumerEnable +
               ", rocketmqEntryPoint='" + rocketmqEntryPoint + '\'' +
               ", basePackages=" + basePackages +
               '}';
    }
}
