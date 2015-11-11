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

package com.navercorp.pinpoint.common;

import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author emeroad
 */
public class HistogramSchemaTest {
    @Test
    public void testAddHistogramSlot() throws Exception {

    }

    @Test
    public void testGetHistogramSlotList() throws Exception {

    }

    @Test
    public void testCreateNode() throws Exception {

    }

    @Test
    public void testFindHistogramSlot() throws Exception {
        HistogramSchema histogramSchema = ServiceType.STAND_ALONE.getHistogramSchema();
        Assert.assertEquals(histogramSchema.findHistogramSlot(999, false).getSlotTime(), 1000);
        Assert.assertEquals(histogramSchema.findHistogramSlot(1000, false).getSlotTime(), 1000);
        Assert.assertEquals(histogramSchema.findHistogramSlot(1111, false).getSlotTime(), 3000);
    }



    @Test
    public void testGetHistogramSlotIndex() throws Exception {

    }
}
