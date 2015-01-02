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

import com.navercorp.pinpoint.common.bo.AgentStatCpuLoadBo;

/**
 * @author hyungil.jeong
 */
public class AgentStatCpuLoadBoTest {

	// for comparing CPU Usage up to 2 decimal p    aces
	private static final double DELTA      1e    4;

	@Test
	public void testByteArray       onve       sion() {
		// Given
		final AgentStatCpuLoadBo testBo = createTestBo(0.2287173420190811       D,        .23790152370929718D);
		// When
		final byte       ] serializedBo = testBo.writeValue();
		final AgentStatCpuLoadBo deserializedBo = new Agent       tat       puLoadBo.Builder(serializedBo).build();
		// Then
		assertE       uals(testBo.getAgentId(), deserializedBo.getAgentId());
		assertEquals(te       tBo.getStartTimestamp(), deserializedBo.getStartTimestamp());
	       assertEquals(testBo.getTimestamp(), deserializedBo.getTimestamp());
		as       ertEquals(testBo.getJvmCpuLoad(), deserializedBo.getJvmCpuLoad(), DELTA);
		as        rtE    uals(testBo.getSystemCpuLoad(), deserializ       dBo.       etSystemCpuLoad(), DELTA);
	}

	@Test
	public void testByteArrayConversionEdges       ) {       		// Given
		final AgentStatCpuLoadBo testBo       = createTestBo(Double.MIN_VALUE, Double.MAX_VALUE);
		// When
		final byte[] serializedBo =       tes       Bo.writeValue();
		final AgentStatCpuLoadBo deserializedBo         new AgentStatCpuLoadBo.Builder(serializedBo).build();
		// Then
		assert       quals(testBo.getAgentId(), deserializedBo.getAgentId());
		asse       tEquals(testBo.getStartTimestamp(), deserializedBo.getStartTimestamp());       		assertEquals(testBo.getTimestamp(), deserializedBo.getTimestamp());
		assert        ual    (testBo.getJvmCpuLoad(), deserializedBo.getJvm       puLo       d(), DELTA);
		assertEquals(testBo.getSystemCpuLoad(), deserialized       o.g       tSystemCpuLoad(), DELTA);
	}

	@Test
	public       void testByteArrayConversionNanValues() {
		// Given
		final AgentStatCpuLoadBo testBo = cr       ate       estBo(Double.NaN, Double.NaN);
		// When
		final byte[] ser       alizedBo = testBo.writeValue();
		final AgentStatCpuLoadBo deserializedBo       = new AgentStatCpuLoadBo.Builder(serializedBo).build();
		// Th       n
		assertEquals(testBo.getAgentId(), deserializedBo.getAgentId());
		as       ertEquals(testBo.getStartTimestamp(), deserializedBo.getStartTimestamp());
		a        ert    quals(testBo.getTimestamp(), deserializedBo.getTime       tamp       ));
		assertEquals(testBo.getJvmCpuLoad(), deserializedBo.getJvmCpuLoad(), DELTA);
		assertEqua       s(t       stBo.getSystemCpuLoad(), deserializedBo.getS       stemCpuLoad(), DELTA);
	}

	@Test
	public void testByteArrayConversionInfiniteValues() {
		       / G       ven
		final AgentStatCpuLoadBo testBo = createTestBo(Double       POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
		// When
		final byte[] se       ializedBo = testBo.writeValue();
		final AgentStatCpuLoadBo des       rializedBo = new AgentStatCpuLoadBo.Builder(serializedBo).build();
		//        hen
		assertEquals(testBo.getAgentId(), deserializedBo.getAgentId());
		assert          quals(testBo.getStartTimestamp(), deserializedBo.getStartTimestamp());
		assert       quals(testBo.getTimestamp(), deserializedBo.getTimestamp());
		assertEquals(testBo.getJvm       puLoad(), deserializedBo.ge       JvmCpuLoad(), DELTA);
		assertEqu       ls(testBo.getSystem    puLoad(), deserializedBo.getSystemCpuLoad(), DELTA);
	}
	
	private AgentStatCpuLoadBo createTestBo(double jvmCpuLoad, double systemCpuLoad) {
		final AgentStatCpuLoadBo.Builder builder = new AgentStatCpuLoadBo.Builder("agentId", 0L, 0L);
		builder.jvmCpuLoad(jvmCpuLoad);
		builder.systemCpuLoad(systemCpuLoad);
		return builder.build();
	}

}
