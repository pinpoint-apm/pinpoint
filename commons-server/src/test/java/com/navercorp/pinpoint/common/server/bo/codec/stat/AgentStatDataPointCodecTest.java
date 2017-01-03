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

package com.navercorp.pinpoint.common.server.bo.codec.stat;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class AgentStatDataPointCodecTest {

    private AgentStatDataPointCodec codec = new AgentStatDataPointCodec();

    @Test
    public void test_timestamps() {
        // Given
        final long initialTimestamp = System.currentTimeMillis();
        final long intervalMs = 5000L;
        final long randomDelta = 10L;
        final int numValues = (int) (Math.random() * 100) + 1;
        final List<Long> expectedTimestamps = createTimestamps(initialTimestamp, intervalMs, randomDelta, numValues);
        final Buffer timestampBuffer = new AutomaticBuffer();
        // When
        codec.encodeTimestamps(timestampBuffer, expectedTimestamps);
        // Then
        List<Long> decodedTimestamps = codec.decodeTimestamps(initialTimestamp, new FixedBuffer(timestampBuffer.getBuffer()), numValues);
        Assert.assertEquals(expectedTimestamps, decodedTimestamps);
    }

    @Test
    public void test_single_timestamp() {
        // Given
        final long givenTimestamp = System.currentTimeMillis();
        final List<Long> expectedTimestamp = Arrays.asList(givenTimestamp);
        final Buffer timestampBuffer = new AutomaticBuffer();
        // When
        codec.encodeTimestamps(timestampBuffer, expectedTimestamp);
        // Then
        List<Long> decodedTimestamp = codec.decodeTimestamps(givenTimestamp, new FixedBuffer(timestampBuffer.getBuffer()), 1);
        Assert.assertEquals(expectedTimestamp, decodedTimestamp);
    }

    private List<Long> createTimestamps(long initialTimestampMs, long intervalMs, long randomDelta, int numValues) {
        List<Long> timestamps = new ArrayList<Long>(numValues);
        timestamps.add(initialTimestampMs);
        long prevTimestamp = initialTimestampMs;
        for (int i = 1; i < numValues; ++i) {
            long delta = ((long) (Math.random() * (randomDelta * 2))) - randomDelta;
            long timestamp = prevTimestamp + (intervalMs + delta);
            timestamps.add(timestamp);
            prevTimestamp = timestamp;
        }
        return timestamps;
    }
}
