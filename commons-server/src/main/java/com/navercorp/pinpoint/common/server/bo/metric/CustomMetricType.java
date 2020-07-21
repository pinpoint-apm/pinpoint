/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.metric;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.LongCounter;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;

import static com.navercorp.pinpoint.common.server.bo.metric.CustomMetricTypeFactory.of;

/**
 * @author Taejin Koo
 */
public interface CustomMetricType {

    AgentStatType getAgentStatType();

    FieldDescriptors getFieldDescriptors();

    CustomMetricType NETTY_DIRECT_BUFFER = of(AgentStatType.NETTY_DIRECT_BUFFER,
            new FieldDescriptor(0, "custom/netty/usedDirectMemory", LongCounter.class),
            new FieldDescriptor(1, "custom/netty/maxDirectMemory", LongCounter.class));

}
