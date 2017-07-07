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

package com.navercorp.pinpoint.web.mapper.stat;

import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.JvmGcSampler;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGc;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author HyunGil Jeong
 */
@RunWith(MockitoJUnitRunner.class)
public class JvmGcSamplerTest {

    private static final Random RANDOM = new Random();

    private final JvmGcSampler sampler = new JvmGcSampler();

    @Test
    public void gcCalculation_singleDataPoint() {
        // Given
        long previousGcCount = randomGcCount();
        long previousGcTime = randomGcTime();
        JvmGcBo previousJvmGcBo = createJvmGcBoForGcTest(previousGcCount, previousGcTime);

        long gcCount = randomGcCount() + previousGcCount;
        long gcTime = randomGcTime() + previousGcTime;
        JvmGcBo jvmGcBo = createJvmGcBoForGcTest(gcCount, gcTime);

        List<JvmGcBo> jvmGcBos = Arrays.asList(jvmGcBo);

        // When
        SampledJvmGc sampledJvmGc = sampler.sampleDataPoints(0, System.currentTimeMillis(), jvmGcBos, previousJvmGcBo);

        // Then
        long expectedGcCount = gcCount - previousGcCount;
        long expectedGcTime = gcTime - previousGcTime;
        long actualGcCount = sampledJvmGc.getGcOldCount().getSumYVal();
        long actualGcTime = sampledJvmGc.getGcOldTime().getSumYVal();
        Assert.assertEquals(expectedGcCount, actualGcCount);
        Assert.assertEquals(expectedGcTime, actualGcTime);
    }

    @Test
    public void gcCalculation_singleDataPoint_noPrevious() {
        // Given
        long gcCount = randomGcCount();
        long gcTime = randomGcTime();
        JvmGcBo jvmGcBo = createJvmGcBoForGcTest(gcCount, gcTime);

        List<JvmGcBo> jvmGcBos = Arrays.asList(jvmGcBo);

        // When
        SampledJvmGc sampledJvmGc = sampler.sampleDataPoints(0, System.currentTimeMillis(), jvmGcBos, null);

        // Then
        long expectedGcCount = 0L;
        long expectedGcTime = 0L;
        long actualGcCount = sampledJvmGc.getGcOldCount().getSumYVal();
        long actualGcTime = sampledJvmGc.getGcOldTime().getSumYVal();
        Assert.assertEquals(expectedGcCount, actualGcCount);
        Assert.assertEquals(expectedGcTime, actualGcTime);
    }

    @Test
    public void gcCalculation_multipleDataPoints() {
        // Given
        long previousGcCount = randomGcCount();
        long previousGcTime = randomGcTime();
        JvmGcBo previousJvmGcBo = createJvmGcBoForGcTest(previousGcCount, previousGcTime);

        long firstGcCount = randomGcCount() + previousGcCount;
        long firstGcTime = randomGcTime() + previousGcTime;
        JvmGcBo firstJvmGcBo = createJvmGcBoForGcTest(firstGcCount, firstGcTime);
        long secondGcCount = randomGcCount() + firstGcCount;
        long secondGcTime = randomGcTime() + firstGcTime;
        JvmGcBo secondJvmGcBo = createJvmGcBoForGcTest(secondGcCount, secondGcTime);

        // must be in descending order
        List<JvmGcBo> jvmGcBos = Arrays.asList(secondJvmGcBo, firstJvmGcBo);

        // When
        SampledJvmGc sampledJvmGc = sampler.sampleDataPoints(0, System.currentTimeMillis(), jvmGcBos, previousJvmGcBo);

        // Then
        long expectedGcCount = secondGcCount - previousGcCount;
        long expectedGcTime = secondGcTime - previousGcTime;
        long actualGcCount = sampledJvmGc.getGcOldCount().getSumYVal();
        long actualGcTime = sampledJvmGc.getGcOldTime().getSumYVal();
        Assert.assertEquals(expectedGcCount, actualGcCount);
        Assert.assertEquals(expectedGcTime, actualGcTime);
    }

    @Test
    public void gcCalculation_multipleDataPoints_noPrevious() {
        // Given
        long firstGcCount = randomGcCount();
        long firstGcTime = randomGcTime();
        JvmGcBo firstJvmGcBo = createJvmGcBoForGcTest(firstGcCount, firstGcTime);
        long secondGcCount = randomGcCount() + firstGcCount;
        long secondGcTime = randomGcTime() + firstGcTime;
        JvmGcBo secondJvmGcBo = createJvmGcBoForGcTest(secondGcCount, secondGcTime);

        // must be in descending order
        List<JvmGcBo> jvmGcBos = Arrays.asList(secondJvmGcBo, firstJvmGcBo);

        // When
        SampledJvmGc sampledJvmGc = sampler.sampleDataPoints(0, System.currentTimeMillis(), jvmGcBos, null);

        // Then
        long expectedGcCount = secondGcCount - firstGcCount;
        long expectedGcTime = secondGcTime - firstGcTime;
        long actualGcCount = sampledJvmGc.getGcOldCount().getSumYVal();
        long actualGcTime = sampledJvmGc.getGcOldTime().getSumYVal();
        Assert.assertEquals(expectedGcCount, actualGcCount);
        Assert.assertEquals(expectedGcTime, actualGcTime);
    }

    @Test
    public void gcCalculation_jvmRestarts() {
        // Given
        long firstAgentStartTimestamp = 10L;
        long secondAgentStartTimestamp = 1000L;

        long previousGcCount = randomGcCount();
        long previousGcTime = randomGcTime();
        JvmGcBo previousJvmGcBo = createJvmGcBoForGcTest(firstAgentStartTimestamp, previousGcCount, previousGcTime);

        long firstGcCount_1 = randomGcCount() + previousGcCount;
        long firstGcTime_1 = randomGcTime() + previousGcTime;
        JvmGcBo firstJvmGcBo_1 = createJvmGcBoForGcTest(firstAgentStartTimestamp, firstGcCount_1, firstGcTime_1);
        long secondGcCount_1 = randomGcCount() + firstGcCount_1;
        long secondGcTime_1 = randomGcTime() + firstGcTime_1;
        JvmGcBo secondJvmGcBo_1 = createJvmGcBoForGcTest(firstAgentStartTimestamp, secondGcCount_1, secondGcTime_1);

        long firstGcCount_2 = randomGcCount();
        long firstGcTime_2 = randomGcTime();
        JvmGcBo firstJvmGcBo_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, firstGcCount_2, firstGcTime_2);
        long secondGcCount_2 = randomGcCount() + firstGcCount_2;
        long secondGcTime_2 = randomGcTime() + firstGcTime_2;
        JvmGcBo secondJvmGcBo_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, secondGcCount_2, secondGcTime_2);

        // must be in descending order
        List<JvmGcBo> jvmGcBos = Arrays.asList(secondJvmGcBo_2, firstJvmGcBo_2, secondJvmGcBo_1, firstJvmGcBo_1);

        // When
        SampledJvmGc sampledJvmGc = sampler.sampleDataPoints(0, System.currentTimeMillis(), jvmGcBos, previousJvmGcBo);

        // Then
        long gcCountsBeforeJvmRestart = secondGcCount_1 - previousGcCount;
        long gcCountsAfterJvmRestart = secondGcCount_2;
        long gcTimesBeforeJvmRestart = secondGcTime_1 - previousGcTime;
        long gcTimesAfterJvmRestart = secondGcTime_2;
        long expectedGcCount = gcCountsBeforeJvmRestart + gcCountsAfterJvmRestart;
        long expectedGcTime = gcTimesBeforeJvmRestart + gcTimesAfterJvmRestart;
        long actualGcCount = sampledJvmGc.getGcOldCount().getSumYVal();
        long actualGcTime = sampledJvmGc.getGcOldTime().getSumYVal();
        Assert.assertEquals(expectedGcCount, actualGcCount);
        Assert.assertEquals(expectedGcTime, actualGcTime);
    }

    @Test
    public void gcCalculation_jvmRestarts_noPrevious() {
        // Given
        long firstAgentStartTimestamp = 10L;
        long secondAgentStartTimestamp = 1000L;

        long firstGcCount_1 = randomGcCount();
        long firstGcTime_1 = randomGcTime();
        JvmGcBo firstJvmGcBo_1 = createJvmGcBoForGcTest(firstAgentStartTimestamp, firstGcCount_1, firstGcTime_1);
        long secondGcCount_1 = randomGcCount() + firstGcCount_1;
        long secondGcTime_1 = randomGcTime() + firstGcTime_1;
        JvmGcBo secondJvmGcBo_1 = createJvmGcBoForGcTest(firstAgentStartTimestamp, secondGcCount_1, secondGcTime_1);

        long firstGcCount_2 = randomGcCount();
        long firstGcTime_2 = randomGcTime();
        JvmGcBo firstJvmGcBo_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, firstGcCount_2, firstGcTime_2);
        long secondGcCount_2 = randomGcCount() + firstGcCount_2;
        long secondGcTime_2 = randomGcTime() + firstGcTime_2;
        JvmGcBo secondJvmGcBo_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, secondGcCount_2, secondGcTime_2);

        // must be in descending order
        List<JvmGcBo> jvmGcBos = Arrays.asList(secondJvmGcBo_2, firstJvmGcBo_2, secondJvmGcBo_1, firstJvmGcBo_1);

        // When
        SampledJvmGc sampledJvmGc = sampler.sampleDataPoints(0, System.currentTimeMillis(), jvmGcBos, null);

        // Then
        long gcCountsBeforeJvmRestart = secondGcCount_1 - firstGcCount_1;
        long gcCountsAfterJvmRestart = secondGcCount_2;
        long gcTimesBeforeJvmRestart = secondGcTime_1 - firstGcTime_1;
        long gcTimesAfterJvmRestart = secondGcTime_2;
        long expectedGcCount = gcCountsBeforeJvmRestart + gcCountsAfterJvmRestart;
        long expectedGcTime = gcTimesBeforeJvmRestart + gcTimesAfterJvmRestart;
        long actualGcCount = sampledJvmGc.getGcOldCount().getSumYVal();
        long actualGcTime = sampledJvmGc.getGcOldTime().getSumYVal();
        Assert.assertEquals(expectedGcCount, actualGcCount);
        Assert.assertEquals(expectedGcTime, actualGcTime);
    }

    @Test
    public void gcCalculation_uncollectedValues() {
        // Given
        long previousGcCount = randomGcCount();
        long previousGcTime = randomGcTime();
        JvmGcBo previousJvmGcBo = createJvmGcBoForGcTest(previousGcCount, previousGcTime);

        JvmGcBo uncollectedJvmGcBo1 = createJvmGcBoForGcTest(JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);
        long firstGcCount = randomGcCount() + previousGcCount;
        long firstGcTime = randomGcTime() + previousGcTime;
        JvmGcBo firstJvmGcBo = createJvmGcBoForGcTest(firstGcCount, firstGcTime);
        JvmGcBo uncollectedJvmGcBo2 = createJvmGcBoForGcTest(JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);
        JvmGcBo uncollectedJvmGcBo3 = createJvmGcBoForGcTest(JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);
        long secondGcCount = randomGcCount() + firstGcCount;
        long secondGcTime = randomGcTime() + firstGcTime;
        JvmGcBo secondJvmGcBo = createJvmGcBoForGcTest(secondGcCount, secondGcTime);

        // must be in descending order
        List<JvmGcBo> jvmGcBos = Arrays.asList(secondJvmGcBo, uncollectedJvmGcBo3, uncollectedJvmGcBo2, firstJvmGcBo, uncollectedJvmGcBo1);

        // When
        SampledJvmGc sampledJvmGc = sampler.sampleDataPoints(0, System.currentTimeMillis(), jvmGcBos, previousJvmGcBo);

        // Then
        long expectedGcCount = secondGcCount - previousGcCount;
        long expectedGcTime = secondGcTime - previousGcTime;
        long actualGcCount = sampledJvmGc.getGcOldCount().getSumYVal();
        long actualGcTime = sampledJvmGc.getGcOldTime().getSumYVal();
        Assert.assertEquals(expectedGcCount, actualGcCount);
        Assert.assertEquals(expectedGcTime, actualGcTime);
    }

    @Test
    public void gcCalculation_uncollectedValues_noPrevious() {
        // Given
        long firstGcCount = randomGcCount();
        long firstGcTime = randomGcTime();
        JvmGcBo firstJvmGcBo = createJvmGcBoForGcTest(firstGcCount, firstGcTime);
        JvmGcBo uncollectedJvmGcBo1 = createJvmGcBoForGcTest(JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);
        JvmGcBo uncollectedJvmGcBo2 = createJvmGcBoForGcTest(JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);
        long secondGcCount = randomGcCount() + firstGcCount;
        long secondGcTime = randomGcTime() + firstGcTime;
        JvmGcBo secondJvmGcBo = createJvmGcBoForGcTest(secondGcCount, secondGcTime);
        JvmGcBo uncollectedJvmGcBo3 = createJvmGcBoForGcTest(JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);
        // must be in descending order
        List<JvmGcBo> jvmGcBos = Arrays.asList(uncollectedJvmGcBo3, secondJvmGcBo, uncollectedJvmGcBo2, uncollectedJvmGcBo1, firstJvmGcBo);

        // When
        SampledJvmGc sampledJvmGc = sampler.sampleDataPoints(0, System.currentTimeMillis(), jvmGcBos, null);

        // Then
        long expectedGcCount = secondGcCount - firstGcCount;
        long expectedGcTime = secondGcTime - firstGcTime;
        long actualGcCount = sampledJvmGc.getGcOldCount().getSumYVal();
        long actualGcTime = sampledJvmGc.getGcOldTime().getSumYVal();
        Assert.assertEquals(expectedGcCount, actualGcCount);
        Assert.assertEquals(expectedGcTime, actualGcTime);

    }

    @Test
    public void gcCalculation_uncollectedValues_previousUncollectedValue() {
        // Given
        JvmGcBo previousJvmGcBo = createJvmGcBoForGcTest(JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);

        JvmGcBo uncollectedJvmGcBo1 = createJvmGcBoForGcTest(JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);
        long firstGcCount = randomGcCount();
        long firstGcTime = randomGcTime();
        JvmGcBo firstJvmGcBo = createJvmGcBoForGcTest(firstGcCount, firstGcTime);
        JvmGcBo uncollectedJvmGcBo2 = createJvmGcBoForGcTest(JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);
        long secondGcCount = randomGcCount() + firstGcCount;
        long secondGcTime = randomGcTime() + firstGcTime;
        JvmGcBo secondJvmGcBo = createJvmGcBoForGcTest(secondGcCount, secondGcTime);

        // must be in descending order
        List<JvmGcBo> jvmGcBos = Arrays.asList(secondJvmGcBo, uncollectedJvmGcBo2, firstJvmGcBo, uncollectedJvmGcBo1);

        // When
        SampledJvmGc sampledJvmGc = sampler.sampleDataPoints(0, System.currentTimeMillis(), jvmGcBos, previousJvmGcBo);

        // Then
        long expectedGcCount = secondGcCount - firstGcCount;
        long expectedGcTime = secondGcTime - firstGcTime;
        long actualGcCount = sampledJvmGc.getGcOldCount().getSumYVal();
        long actualGcTime = sampledJvmGc.getGcOldTime().getSumYVal();
        Assert.assertEquals(expectedGcCount, actualGcCount);
        Assert.assertEquals(expectedGcTime, actualGcTime);
    }

    @Test
    public void gcCalculation_jvmRestarts_uncollectedValues() {
        // Given
        long firstAgentStartTimestamp = 10L;
        long secondAgentStartTimestamp = 1000L;

        long previousGcCount = randomGcCount();
        long previousGcTime = randomGcTime();
        JvmGcBo previousJvmGcBo = createJvmGcBoForGcTest(firstAgentStartTimestamp, previousGcCount, previousGcTime);

        JvmGcBo uncollectedJvmGcBo1_1 = createJvmGcBoForGcTest(firstAgentStartTimestamp, JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);
        long firstGcCount_1 = randomGcCount() + previousGcCount;
        long firstGcTime_1 = randomGcTime() + previousGcTime;
        JvmGcBo firstJvmGcBo_1 = createJvmGcBoForGcTest(firstAgentStartTimestamp, firstGcCount_1, firstGcTime_1);
        JvmGcBo uncollectedJvmGcBo2_1 = createJvmGcBoForGcTest(firstAgentStartTimestamp, JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);

        JvmGcBo uncollectedJvmGcBo1_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);
        long firstGcCount_2 = randomGcCount();
        long firstGcTime_2 = randomGcTime();
        JvmGcBo firstJvmGcBo_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, firstGcCount_2, firstGcTime_2);
        long secondGcCount_2 = randomGcCount() + firstGcCount_2;
        long secondGcTime_2 = randomGcTime() + firstGcTime_2;
        JvmGcBo secondJvmGcBo_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, secondGcCount_2, secondGcTime_2);
        long thirdGcCount_2 = randomGcCount() + secondGcCount_2;
        long thirdGcTime_2 = randomGcCount() + secondGcTime_2;
        JvmGcBo thirdJvmGcBo_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, thirdGcCount_2, thirdGcTime_2);
        JvmGcBo uncollectedJvmGcBo2_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);

        // must be in descending order
        List<JvmGcBo> jvmGcBos = Arrays.asList(
                uncollectedJvmGcBo2_2, thirdJvmGcBo_2, secondJvmGcBo_2, firstJvmGcBo_2, uncollectedJvmGcBo1_2,
                uncollectedJvmGcBo2_1, firstJvmGcBo_1, uncollectedJvmGcBo1_1
        );

        // When
        SampledJvmGc sampledJvmGc = sampler.sampleDataPoints(0, System.currentTimeMillis(), jvmGcBos, previousJvmGcBo);

        // Then
        long gcCountsBeforeJvmRestart = firstGcCount_1 - previousGcCount;
        long gcCountsAfterJvmRestart = thirdGcCount_2;
        long gcTimesBeforeJvmRestart = firstGcTime_1 - previousGcTime;
        long gcTimesAfterJvmRestart = thirdGcTime_2;
        long expectedGcCount = gcCountsBeforeJvmRestart + gcCountsAfterJvmRestart;
        long expectedGcTime = gcTimesBeforeJvmRestart + gcTimesAfterJvmRestart;
        long actualGcCount = sampledJvmGc.getGcOldCount().getSumYVal();
        long actualGcTime = sampledJvmGc.getGcOldTime().getSumYVal();
        Assert.assertEquals(expectedGcCount, actualGcCount);
        Assert.assertEquals(expectedGcTime, actualGcTime);
    }

    @Test
    public void gcCalculation_jvmRestarts_uncollectedValues_noPrevious() {
        // Given
        long firstAgentStartTimestamp = 10L;
        long secondAgentStartTimestamp = 1000L;

        JvmGcBo uncollectedJvmGcBo1_1 = createJvmGcBoForGcTest(firstAgentStartTimestamp, JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);
        long firstGcCount_1 = randomGcCount();
        long firstGcTime_1 = randomGcTime();
        JvmGcBo firstJvmGcBo_1 = createJvmGcBoForGcTest(firstAgentStartTimestamp, firstGcCount_1, firstGcTime_1);
        JvmGcBo uncollectedJvmGcBo2_1 = createJvmGcBoForGcTest(firstAgentStartTimestamp, JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);

        long firstGcCount_2 = randomGcCount();
        long firstGcTime_2 = randomGcTime();
        JvmGcBo firstJvmGcBo_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, firstGcCount_2, firstGcTime_2);
        JvmGcBo uncollectedJvmGcBo1_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);
        long secondGcCount_2 = randomGcCount() + firstGcCount_2;
        long secondGcTime_2 = randomGcTime() + firstGcTime_2;
        JvmGcBo secondJvmGcBo_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, secondGcCount_2, secondGcTime_2);
        long thirdGcCount_2 = randomGcCount() + secondGcCount_2;
        long thirdGcTime_2 = randomGcCount() + secondGcTime_2;
        JvmGcBo thirdJvmGcBo_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, thirdGcCount_2, thirdGcTime_2);
        JvmGcBo uncollectedJvmGcBo2_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);

        // must be in descending order
        List<JvmGcBo> jvmGcBos = Arrays.asList(
                uncollectedJvmGcBo2_2, thirdJvmGcBo_2, secondJvmGcBo_2, uncollectedJvmGcBo1_2, firstJvmGcBo_2,
                uncollectedJvmGcBo2_1, firstJvmGcBo_1, uncollectedJvmGcBo1_1
        );

        // When
        SampledJvmGc sampledJvmGc = sampler.sampleDataPoints(0, System.currentTimeMillis(), jvmGcBos, null);

        // Then
        long gcCountsBeforeJvmRestart = 0L;
        long gcCountsAfterJvmRestart = thirdGcCount_2;
        long gcTimesBeforeJvmRestart = 0L;
        long gcTimesAfterJvmRestart = thirdGcTime_2;
        long expectedGcCount = gcCountsBeforeJvmRestart + gcCountsAfterJvmRestart;
        long expectedGcTime = gcTimesBeforeJvmRestart + gcTimesAfterJvmRestart;
        long actualGcCount = sampledJvmGc.getGcOldCount().getSumYVal();
        long actualGcTime = sampledJvmGc.getGcOldTime().getSumYVal();
        Assert.assertEquals(expectedGcCount, actualGcCount);
        Assert.assertEquals(expectedGcTime, actualGcTime);
    }

    @Test
    public void gcCalculation_jvmRestarts_uncollectedValues_previousUncollectedValue() {
        // Given
        long firstAgentStartTimestamp = 10L;
        long secondAgentStartTimestamp = 1000L;

        JvmGcBo previousJvmGcBo = createJvmGcBoForGcTest(firstAgentStartTimestamp, JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);

        JvmGcBo uncollectedJvmGcBo1_1 = createJvmGcBoForGcTest(firstAgentStartTimestamp, JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);
        long firstGcCount_1 = randomGcCount();
        long firstGcTime_1 = randomGcTime();
        JvmGcBo firstJvmGcBo_1 = createJvmGcBoForGcTest(firstAgentStartTimestamp, firstGcCount_1, firstGcTime_1);
        long secondGcCount_1 = randomGcCount() + firstGcCount_1;
        long secondGcTime_1 = randomGcTime() + firstGcTime_1;
        JvmGcBo secondJvmGcBo_1 = createJvmGcBoForGcTest(firstAgentStartTimestamp, secondGcCount_1, secondGcTime_1);
        JvmGcBo uncollectedJvmGcBo2_1 = createJvmGcBoForGcTest(firstAgentStartTimestamp, JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);

        long firstGcCount_2 = randomGcCount();
        long firstGcTime_2 = randomGcTime();
        JvmGcBo firstJvmGcBo_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, firstGcCount_2, firstGcTime_2);
        JvmGcBo uncollectedJvmGcBo1_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);
        long secondGcCount_2 = randomGcCount() + firstGcCount_2;
        long secondGcTime_2 = randomGcTime() + firstGcTime_2;
        JvmGcBo secondJvmGcBo_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, secondGcCount_2, secondGcTime_2);
        long thirdGcCount_2 = randomGcCount() + secondGcCount_2;
        long thirdGcTime_2 = randomGcCount() + secondGcTime_2;
        JvmGcBo thirdJvmGcBo_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, thirdGcCount_2, thirdGcTime_2);
        JvmGcBo uncollectedJvmGcBo2_2 = createJvmGcBoForGcTest(secondAgentStartTimestamp, JvmGcBo.UNCOLLECTED_VALUE, JvmGcBo.UNCOLLECTED_VALUE);

        // must be in descending order
        List<JvmGcBo> jvmGcBos = Arrays.asList(
                uncollectedJvmGcBo2_2, thirdJvmGcBo_2, secondJvmGcBo_2, uncollectedJvmGcBo1_2, firstJvmGcBo_2,
                uncollectedJvmGcBo2_1, secondJvmGcBo_1, firstJvmGcBo_1, uncollectedJvmGcBo1_1
        );

        // When
        SampledJvmGc sampledJvmGc = sampler.sampleDataPoints(0, System.currentTimeMillis(), jvmGcBos, previousJvmGcBo);

        // Then
        long gcCountsBeforeJvmRestart = secondGcCount_1 - firstGcCount_1;
        long gcCountsAfterJvmRestart = thirdGcCount_2;
        long gcTimesBeforeJvmRestart = secondGcTime_1 - firstGcTime_1;
        long gcTimesAfterJvmRestart = thirdGcTime_2;
        long expectedGcCount = gcCountsBeforeJvmRestart + gcCountsAfterJvmRestart;
        long expectedGcTime = gcTimesBeforeJvmRestart + gcTimesAfterJvmRestart;
        long actualGcCount = sampledJvmGc.getGcOldCount().getSumYVal();
        long actualGcTime = sampledJvmGc.getGcOldTime().getSumYVal();
        Assert.assertEquals(expectedGcCount, actualGcCount);
        Assert.assertEquals(expectedGcTime, actualGcTime);

    }

    private JvmGcBo createJvmGcBoForGcTest(long gcOldCount, long gcOldTime) {
        JvmGcBo jvmGcBo = new JvmGcBo();
        jvmGcBo.setGcOldCount(gcOldCount);
        jvmGcBo.setGcOldTime(gcOldTime);
        return jvmGcBo;
    }

    private JvmGcBo createJvmGcBoForGcTest(long startTimestamp, long gcOldCount, long gcOldTime) {
        JvmGcBo jvmGcBo = createJvmGcBoForGcTest(gcOldCount, gcOldTime);
        jvmGcBo.setStartTimestamp(startTimestamp);
        return jvmGcBo;
    }

    private int randomGcCount() {
        return RANDOM.nextInt(10);
    }

    private int randomGcTime() {
        return RANDOM.nextInt(10000);
    }
}
