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

package com.navercorp.pinpoint.profiler.context.provider.thrift;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.context.module.ApplicationName;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.context.thrift.MetadataMessageConverter;
import org.apache.thrift.TBase;


/**
 * @author Woonduk Kang(emeroad)
 */
public class MetadataMessageConverterProvider implements Provider<MessageConverter<TBase<?, ?>>> {

    private final String applicationName;
    private final String agentId;
    private final long agentStartTime;

    @Inject
    public MetadataMessageConverterProvider(@ApplicationName String applicationName, @AgentId String agentId, @AgentStartTime long agentStartTime) {
        this.applicationName = Assert.requireNonNull(applicationName, "applicationName");
        this.agentId = Assert.requireNonNull(agentId, "agentId");
        this.agentStartTime = agentStartTime;
    }


    @Override
    public MessageConverter<TBase<?, ?>> get() {
        MessageConverter<TBase<?, ?>> messageConverter = new MetadataMessageConverter(applicationName, agentId, agentStartTime);
        return messageConverter;
    }
}
