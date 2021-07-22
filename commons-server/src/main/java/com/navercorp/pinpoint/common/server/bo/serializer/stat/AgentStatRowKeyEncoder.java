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

package com.navercorp.pinpoint.common.server.bo.serializer.stat;

import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.navercorp.pinpoint.common.hbase.HbaseTableConstants.AGENT_ID_MAX_LEN;

/**
 * @author HyunGil Jeong
 */
@Component
public class AgentStatRowKeyEncoder implements RowKeyEncoder<AgentStatRowKeyComponent> {

    @Override
    public byte[] encodeRowKey(AgentStatRowKeyComponent component) {
        Objects.requireNonNull(component, "component");

        byte[] bAgentId = BytesUtils.toBytes(component.getAgentId());
        byte[] bStatType = new byte[]{component.getAgentStatType().getRawTypeCode()};
        byte[] rowKey = new byte[AGENT_ID_MAX_LEN + bStatType.length + BytesUtils.LONG_BYTE_LENGTH];

        BytesUtils.writeBytes(rowKey, 0, bAgentId);
        BytesUtils.writeBytes(rowKey, AGENT_ID_MAX_LEN, bStatType);
        BytesUtils.writeLong(TimeUtils.reverseTimeMillis(component.getBaseTimestamp()), rowKey, AGENT_ID_MAX_LEN + bStatType.length);

        return rowKey;
    }
}
