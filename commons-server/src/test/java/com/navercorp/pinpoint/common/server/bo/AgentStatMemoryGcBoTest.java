/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo;

import static org.junit.Assert.*;

import org.junit.Test;

import com.navercorp.pinpoint.common.server.bo.AgentStatMemoryGcBo;
import com.navercorp.pinpoint.thrift.dto.TJvmGcType;

/**
 * @author hyungil.jeong
 */
public class AgentStatMemoryGcBoTest {

    @Test
    public void testByteArrayConversion() {
        // Given
        final AgentStatMemoryGcBo.Builder builder = new AgentStatMemoryGcBo.Builder("agentId", 0L, 1L);
        builder.gcType(TJvmGcType.G1.name());
        builder.jvmMemoryHeapUsed(Long.MIN_VALUE);
        builder.jvmMemoryHeapMax(Long.MAX_VALUE);
        builder.jvmMemoryNonHeapUsed(Long.MIN_VALUE);
        builder.jvmMemoryNonHeapMax(Long.MAX_VALUE);
        builder.jvmGcOldCount(1L);
        builder.jvmGcOldTime(2L);
        final AgentStatMemoryGcBo testBo = builder.build();
        // When
        final byte[] serializedBo = testBo.writeValue();
        final AgentStatMemoryGcBo deserializedBo = new AgentStatMemoryGcBo.Builder(serializedBo).build();
        // Then
        assertEquals(testBo.getAgentId(), deserializedBo.getAgentId());
        assertEquals(testBo.getStartTimestamp(), deserializedBo.getStartTimestamp());
        assertEquals(testBo.getTimestamp(), deserializedBo.getTimestamp());
        assertEquals(testBo.getGcType(), deserializedBo.getGcType());
        assertEquals(testBo.getJvmMemoryHeapUsed(), deserializedBo.getJvmMemoryHeapUsed());
        assertEquals(testBo.getJvmMemoryHeapMax(), deserializedBo.getJvmMemoryHeapMax());
        assertEquals(testBo.getJvmMemoryNonHeapUsed(), deserializedBo.getJvmMemoryNonHeapUsed());
        assertEquals(testBo.getJvmMemoryNonHeapMax(), deserializedBo.getJvmMemoryNonHeapMax());
        assertEquals(testBo.getJvmGcOldCount(), deserializedBo.getJvmGcOldCount());
        assertEquals(testBo.getJvmGcOldTime(), deserializedBo.getJvmGcOldTime());
    }

}
