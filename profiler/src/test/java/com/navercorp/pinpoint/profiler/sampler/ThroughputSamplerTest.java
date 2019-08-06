/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.sampler;

import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.bootstrap.sampler.TraceSampler;
import com.navercorp.pinpoint.profiler.context.id.AtomicIdGenerator;
import com.navercorp.pinpoint.profiler.context.id.IdGenerator;
import org.junit.Assert;
import org.junit.Test;

public class ThroughputSamplerTest {

    @Test
    public void isSampling_1() {
        // 1 per seconds
        TraceSampler sampler = newTraceSampler(1);
        Assert.assertTrue(sampler.isNewSampled().isSampled());

    }

    @Test
    public void isSampling_1000() {
        // 1000 per seconds
        TraceSampler sampler = newTraceSampler(1000);
        Assert.assertTrue(sampler.isNewSampled().isSampled());
    }


    private TraceSampler newTraceSampler(int throughput) {
        IdGenerator atomicIdGenerator = new AtomicIdGenerator();
        Sampler trueSampler = new TrueSampler();
        TraceSampler basicSampler = new BasicTraceSampler(atomicIdGenerator, trueSampler);
        return new RateLimitTraceSampler(throughput, 0, atomicIdGenerator, basicSampler);
    }
}