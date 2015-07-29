/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase.mapper;

import org.springframework.stereotype.Component;

import com.navercorp.pinpoint.common.bo.AgentLifeCycleBo;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.ValueMapper;

/**
 * @author HyunGil Jeong
 */
@Component
public class AgentLifeCycleValueMapper implements ValueMapper<AgentLifeCycleBo> {

    @Override
    public byte[] mapValue(AgentLifeCycleBo value) {
        final Buffer buffer = new AutomaticBuffer();
        buffer.put(value.getVersion());
        buffer.putPrefixedString(value.getAgentId());
        buffer.put(value.getStartTimestamp());
        buffer.put(value.getEventTimestamp());
        buffer.put(value.getEventIdentifier());
        buffer.put(value.getAgentLifeCycleState().getCode());
        return buffer.getBuffer();
    }

}
