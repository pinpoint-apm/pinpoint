/*
 * Copyright 2019 NAVER Corp.
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
 */

package com.navercorp.pinpoint.collector.service.async;

import com.navercorp.pinpoint.rpc.server.ChannelProperties;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class AgentPropertyChannelAdaptor implements AgentProperty {

    private final ChannelProperties channelProperties;

    public AgentPropertyChannelAdaptor(ChannelProperties channelProperties) {
        this.channelProperties = Objects.requireNonNull(channelProperties, "channelProperties");
    }

    @Override
    public String getApplicationName() {
        return channelProperties.getApplicationName();
    }

    @Override
    public String getAgentId() {
        return channelProperties.getAgentId();
    }

    @Override
    public long getStartTime() {
        return channelProperties.getStartTime();
    }

    @Override
    public Object get(String key) {
        return channelProperties.get(key);
    }

    @Override
    public short getServiceType() {
        return (short) channelProperties.getServiceType();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentPropertyChannelAdaptor{");
        sb.append("channelProperties=").append(channelProperties);
        sb.append('}');
        return sb.toString();
    }
}