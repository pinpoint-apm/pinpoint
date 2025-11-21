/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.rawdata;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author emeroad
 */
public class AgentHistogramTest {

    private final Logger logger = LogManager.getLogger(this.getClass());


    @Test
    public void test() throws Exception {
        AgentHistogram.Builder builder = AgentHistogram.newBuilder(new Application("test", ServiceType.STAND_ALONE));

        TimeHistogram histogram = new TimeHistogram(ServiceType.STAND_ALONE, 0);
        histogram.addCallCount(ServiceType.STAND_ALONE.getHistogramSchema().getFastErrorSlot().getSlotTime(), 1);
        builder.addTimeHistogram(histogram);

        AgentHistogram copy = builder.build();

        Assertions.assertEquals(1, copy.getHistogram().getTotalErrorCount());

    }


}
