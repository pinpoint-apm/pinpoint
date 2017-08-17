/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.active;

import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ActiveTraceHistogramUtilsTest {
    @Test
    public void asList_ZERO_CASE() throws Exception {

        ActiveTraceHistogram emptyHistogram= new EmptyActiveTraceHistogram(BaseHistogramSchema.NORMAL_SCHEMA);

        List<Integer> zeroList = ActiveTraceHistogramUtils.asList(emptyHistogram);
        Assert.assertEquals(zeroList.size(), 4);
        Assert.assertEquals(emptyHistogram.getFastCount(), 0);
        Assert.assertEquals(emptyHistogram.getNormalCount(), 0);
        Assert.assertEquals(emptyHistogram.getSlowCount(), 0);
        Assert.assertEquals(emptyHistogram.getVerySlowCount(), 0);
    }

}