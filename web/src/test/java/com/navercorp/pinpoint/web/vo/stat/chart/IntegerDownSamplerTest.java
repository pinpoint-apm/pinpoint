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

package com.navercorp.pinpoint.web.vo.stat.chart;

import org.junit.Assert;

/**
 * @author HyunGil Jeong
 */
public class IntegerDownSamplerTest extends DownSamplerTestBase<Integer> {

    @Override
    protected DownSampler<Integer> getSampler() {
        return DownSamplers.getIntegerDownSampler(DEFAULT_VALUE);
    }

    @Override
    protected Integer createSample() {
        return RANDOM.nextInt();
    }

    @Override
    protected void assertEquals(Integer expected, Integer actual) {
        Assert.assertEquals(expected, actual);
    }
}
