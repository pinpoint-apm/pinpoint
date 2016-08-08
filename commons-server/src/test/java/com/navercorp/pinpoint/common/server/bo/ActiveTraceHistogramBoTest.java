/*
 * Copyright 2016 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.ActiveTraceHistogramBo;
import com.navercorp.pinpoint.common.trace.SlotType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class ActiveTraceHistogramBoTest {

    @Test
    public void writeValue_should_be_reconstructed_correctly() {
        // Given
        final int validVersion = 0;
        final int expectedHistogramSchemaType = 1;
        final List<Integer> expectedActiveTraceCounts = Arrays.asList(0, 1, 2, 3);
        // When
        ActiveTraceHistogramBo expectedBo = new ActiveTraceHistogramBo(validVersion, expectedHistogramSchemaType, expectedActiveTraceCounts);
        byte[] serializedBo = expectedBo.writeValue();
        ActiveTraceHistogramBo deserializedBo = new ActiveTraceHistogramBo(serializedBo);
        // Then
        assertEquals(expectedBo, deserializedBo);
    }

    @Test
    public void unsupported_version_should_return_empty_bo() {
        // Given
        final int unsupportedVersion = 255;
        final int expectedHistogramSchemaType = 1;
        // When
        ActiveTraceHistogramBo givenBo = new ActiveTraceHistogramBo(unsupportedVersion, expectedHistogramSchemaType, Arrays.asList(0, 1));
        byte[] serializedBo = givenBo.writeValue();
        ActiveTraceHistogramBo deserializedBo = new ActiveTraceHistogramBo(serializedBo);
        // Then
        assertEquals(unsupportedVersion, deserializedBo.getVersion());
        assertEquals(expectedHistogramSchemaType, deserializedBo.getHistogramSchemaType());
        assertEquals(Collections.emptyMap(), deserializedBo.getActiveTraceCountMap());
    }

    @Test
    public void null_activeTraceCounts_should_return_valid_map_per_version() {
        // Given
        final int validVersion = 0;
        final int expectedHistogramSchemaType = 1;
        final Map<SlotType, Integer> expectedActiveTraceCountMap = new HashMap<SlotType, Integer>();
        expectedActiveTraceCountMap.put(SlotType.FAST, 0);
        expectedActiveTraceCountMap.put(SlotType.NORMAL, 0);
        expectedActiveTraceCountMap.put(SlotType.SLOW, 0);
        expectedActiveTraceCountMap.put(SlotType.VERY_SLOW, 0);
        // When
        ActiveTraceHistogramBo expectedBo = new ActiveTraceHistogramBo(validVersion, expectedHistogramSchemaType, null);
        byte[] serializedBo = expectedBo.writeValue();
        ActiveTraceHistogramBo deserializedBo = new ActiveTraceHistogramBo(serializedBo);
        // Then
        assertEquals(validVersion, deserializedBo.getVersion());
        assertEquals(expectedHistogramSchemaType, deserializedBo.getHistogramSchemaType());
        assertEquals(expectedActiveTraceCountMap, deserializedBo.getActiveTraceCountMap());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalid_version_should_throw_illegalArgumentException() {
        // Given
        final int invalidVersion = -1;
        // When
        new ActiveTraceHistogramBo(invalidVersion, 1, Arrays.asList(0, 1));
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalid_activeTraceCounts_should_throw_illegalArgumentException() {
        // Given
        final int validVersion = 0;
        final int expectedHistogramSchemaType = 1;
        final List<Integer> invalidActiveTraceCounts = Arrays.asList(1, 10, 11);
        // When
        new ActiveTraceHistogramBo(validVersion, expectedHistogramSchemaType, invalidActiveTraceCounts);
        fail();
    }

}
