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

package com.navercorp.pinpoint.collector.mapper.thrift.stat;

import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceHistogram;
import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.thrift.dto.TActiveTrace;
import com.navercorp.pinpoint.thrift.dto.TActiveTraceHistogram;
import org.junit.jupiter.api.Assertions;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class ThriftActiveTraceBoMapperTest extends ThriftBoMapperTestBase<TActiveTrace, ActiveTraceBo> {

    @Override
    protected TActiveTrace create() {
        TActiveTraceHistogram activeTraceHistogram = new TActiveTraceHistogram();
        if (getRandomBoolean()) {
            activeTraceHistogram.setVersion((short) 1);
        } else {
            activeTraceHistogram.setVersion((short) 2);
        }

        if (getRandomBoolean()) {
            activeTraceHistogram.setHistogramSchemaType(BaseHistogramSchema.NORMAL_SCHEMA.getTypeCode());
        } else {
            activeTraceHistogram.setHistogramSchemaType(BaseHistogramSchema.FAST_SCHEMA.getTypeCode());
        }
        activeTraceHistogram.setActiveTraceCount(List.of(
                getRandomInteger(0, Integer.MAX_VALUE),
                getRandomInteger(0, Integer.MAX_VALUE),
                getRandomInteger(0, Integer.MAX_VALUE),
                getRandomInteger(0, Integer.MAX_VALUE)
        ));

        TActiveTrace activeTrace = new TActiveTrace();
        activeTrace.setHistogram(activeTraceHistogram);

        return activeTrace;
    }

    @Override
    protected ActiveTraceBo convert(TActiveTrace original) {
        ThriftActiveTraceBoMapper activeTraceBoMapper = new ThriftActiveTraceBoMapper();
        return activeTraceBoMapper.map(original);
    }

    @Override
    protected void verify(TActiveTrace original, ActiveTraceBo mappedStatDataPoint) {
        Assertions.assertEquals(original.getHistogram().getVersion(), mappedStatDataPoint.getVersion(), "version");
        Assertions.assertEquals(original.getHistogram().getHistogramSchemaType(), mappedStatDataPoint.getHistogramSchemaType(), "schemaType");

        List<Integer> activeTraceCountList = original.getHistogram().getActiveTraceCount();
        for (int i = 0; i < activeTraceCountList.size(); i++) {
            int activeTraceCount = activeTraceCountList.get(i);
            final ActiveTraceHistogram activeTraceHistogram = mappedStatDataPoint.getActiveTraceHistogram();
            if (i == 0) {
                Assertions.assertEquals(activeTraceCount, activeTraceHistogram.getFastCount(), "FAST");
            } else if (i == 1) {
                Assertions.assertEquals(activeTraceCount, activeTraceHistogram.getNormalCount(), "NORMAL");
            } else if (i == 2) {
                Assertions.assertEquals(activeTraceCount, activeTraceHistogram.getSlowCount(), "SLOW");
            } else if (i == 3) {
                Assertions.assertEquals(activeTraceCount, activeTraceHistogram.getVerySlowCount(), "VERY_SLOW");
            } else {
                Assertions.fail();
            }
        }
    }

}
