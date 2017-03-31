/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.instrument.transformer;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import org.junit.Test;

import java.lang.instrument.ClassFileTransformer;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class MatchableTransformerRegistryTest {

    @Test
    public void accumulatorTime() throws Exception {
        IndexValue value = new IndexValue(null, null);
        long startTime = System.currentTimeMillis();
        Thread.sleep(10);
        value.accumulatorTime(startTime);
    }


    class IndexValue {
        final MatcherOperand operand;
        final ClassFileTransformer transformer;
        final AtomicLong accumulatorTimeMillis = new AtomicLong(0);

        public IndexValue(final MatcherOperand operand, final ClassFileTransformer transformer) {
            this.operand = operand;
            this.transformer = transformer;
        }

        public long accumulatorTime(final long startTimeMillis) {
            final long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
            return accumulatorTimeMillis.addAndGet(elapsedTimeMillis);
        }
    }

}