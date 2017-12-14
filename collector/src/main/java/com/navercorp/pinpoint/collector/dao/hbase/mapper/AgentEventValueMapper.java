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

import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.ValueMapper;

/**
 * @author HyunGil Jeong
 */
@Component
public class AgentEventValueMapper implements ValueMapper<AgentEventBo> {

    @Override
    public byte[] mapValue(AgentEventBo value) {
        final Buffer buffer = new AutomaticBuffer();
        buffer.putInt(value.getVersion());
        buffer.putPrefixedString(value.getAgentId());
        buffer.putLong(value.getStartTimestamp());
        buffer.putLong(value.getEventTimestamp());
        buffer.putPrefixedBytes(value.getEventBody());
        return buffer.getBuffer();
    }

}
