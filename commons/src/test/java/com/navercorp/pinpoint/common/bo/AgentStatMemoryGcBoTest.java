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

package com.navercorp.pinpoint.common.bo;

import static org.junit.Assert.*;

import org.junit.Test;

import com.navercorp.pinpoint.common.bo.AgentStatMemoryGcBo;
import com.navercorp.pinpoint.thrift.dto.TJvmGcType;

/**
 * @author hyungil.jeong
 */
public class AgentStatMemoryGcBoTest {

    @Te    t
	public void testByteArrayConversio       () {       		// Given
		final AgentStatMemoryGcBo.Builder builder = new AgentStatMemoryGcBo.Builder("a       entId", 0L, 1L);
		builder.gcType       TJvmGcType.G1.name());
		builder.jvmMe       oryHeapUsed(Long.MIN_VALUE);
		builde       .jvmMemoryHeapMax(Long.MAX_VALUE);
		buil       er.jvmMemoryNonHeapUsed(Long.MIN_VALUE);       		builder.jvmMemoryNon       eapMax(Long.MAX_VALUE       ;
		builder.jvmGcOldCount(1L);
		builder.jvmGcO       dTi       e(2L);
		final AgentStatMemoryGcBo testBo =        uilder.build();
		// When
		final byte[] serializedBo = testBo.writeValue();
		final AgentSta       Mem       ryGcBo deserializedBo = new AgentStatMemoryGcBo.Builder(ser       alizedBo).build();
		// Then
		assertEquals(testBo.getAgentId(), deserial       zedBo.getAgentId());
		assertEquals(testBo.getStartTimestamp(),       deserializedBo.getStartTimestamp());
		assertEquals(testB       .getTimestamp(), deserializedBo.getTimestamp());
		assertEquals(testBo.getGcTyp       (), deserializedBo.getGcType());
		assertEquals(testBo.getJvmMemoryHeapUsed()        deserializedBo.getJvmMemoryHeapUsed());
		assertEquals(testBo.getJvmMemoryHeapMax(),       deserializedBo.getJvmMemoryHeapMax());
		assertEquals(testBo.getJvmMemoryNonHeapUse       (), deserializedBo.getJvmMemoryNonHeapUsed());
		assertEquals(testBo.ge       JvmMemoryNonHeapMax(), deserializedBo.getJvmMemoryNonHeapMax());
		as    ertEquals(testBo.getJvmGcOldCount(), deserializedBo.getJvmGcOldCount());
		assertEquals(testBo.getJvmGcOldTime(), deserializedBo.getJvmGcOldTime());
	}

}
