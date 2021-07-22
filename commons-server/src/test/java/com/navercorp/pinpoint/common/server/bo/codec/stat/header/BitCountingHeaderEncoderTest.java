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

package com.navercorp.pinpoint.common.server.bo.codec.stat.header;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author HyunGil Jeong
 */
public class BitCountingHeaderEncoderTest {

    private static final int MAX_NUM_TEST_VALUES = 20 + 1; // Random API's upper bound field is exclusive

    private static final Random RANDOM = new Random();

    @Test
    public void test_with_random_codes() {
        // Given
        final int numCodes = RandomUtils.nextInt(1, MAX_NUM_TEST_VALUES);
        final List<Integer> givenCodes = new ArrayList<Integer>(numCodes);
        for (int i = 0; i < numCodes; i++) {
            givenCodes.add(RANDOM.nextInt(5));
        }
        // When
        AgentStatHeaderEncoder encoder = new BitCountingHeaderEncoder();
        for (int i = 0; i < givenCodes.size(); i++) {
            encoder.addCode(givenCodes.get(i));
        }
        final byte[] header = encoder.getHeader();
        // Then
        List<Integer> decodedCodes = new ArrayList<Integer>(numCodes);
        AgentStatHeaderDecoder decoder = new BitCountingHeaderDecoder(header);
        for (int i = 0; i < numCodes; i++) {
            int code = decoder.getCode();
            decodedCodes.add(code);
        }
        Assert.assertEquals(givenCodes, decodedCodes);
    }

    @Test
    public void test_zeroes() {
        // Given
        final int numCodes = RandomUtils.nextInt(1, MAX_NUM_TEST_VALUES);
        // When
        AgentStatHeaderEncoder encoder = new BitCountingHeaderEncoder();
        for (int i = 0; i < numCodes; i++) {
            encoder.addCode(0);
        }
        final byte[] header = encoder.getHeader();
        // Then
        AgentStatHeaderDecoder decoder = new BitCountingHeaderDecoder(header);
        for (int i = 0; i < numCodes; i++) {
            Assert.assertEquals(0, decoder.getCode());
        }
    }

    @Test
    public void test_zeroes_followed_by_random_codes() {
        // Given
        final int numZeroes = RandomUtils.nextInt(1, MAX_NUM_TEST_VALUES);
        final int numRandomCodes = RandomUtils.nextInt(1, MAX_NUM_TEST_VALUES);
        final int numTotalCodes = numZeroes + numRandomCodes;
        List<Integer> givenCodes = new ArrayList<Integer>(numTotalCodes);
        for (int i = 0; i < numZeroes; i++) {
            givenCodes.add(0);
        }
        for (int i = 0; i < numRandomCodes; i++) {
            givenCodes.add(RANDOM.nextInt(5));
        }
        // When
        AgentStatHeaderEncoder encoder = new BitCountingHeaderEncoder();
        for (int expectedCode : givenCodes) {
            encoder.addCode(expectedCode);
        }
        final byte[] header = encoder.getHeader();
        // Then
        AgentStatHeaderDecoder decoder = new BitCountingHeaderDecoder(header);
        List<Integer> decodedCodes = new ArrayList<Integer>(numTotalCodes);
        for (int i = 0; i < numTotalCodes; i++) {
            decodedCodes.add(decoder.getCode());
        }
        Assert.assertEquals(givenCodes, decodedCodes);
    }

    @Test
    public void test_empty_codes() {
        AgentStatHeaderEncoder encoder = new BitCountingHeaderEncoder();
        final byte[] header = encoder.getHeader();
        AgentStatHeaderDecoder decoder = new BitCountingHeaderDecoder(header);
        Assert.assertEquals(0, decoder.getCode());
    }
}
