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

import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.thrift.dto.TResponseTime;
import org.junit.Assert;

/**
 * @author Taejin Koo
 */
public class ThriftResponseTimeBoMapperTest extends ThriftBoMapperTestBase<TResponseTime, ResponseTimeBo> {

    private static final long MAX_AVG = Long.MAX_VALUE - 100;

    @Override
    protected TResponseTime create() {
        TResponseTime responseTime = new TResponseTime();
        long randomAvg = getRandomLong(0, MAX_AVG);
        responseTime.setAvg(randomAvg);

        long randomAdditionalMax = getRandomLong(0, 100);
        responseTime.setMax(randomAvg + randomAdditionalMax);
        return responseTime;
    }

    @Override
    protected ResponseTimeBo convert(TResponseTime original) {
        ThriftResponseTimeBoMapper mapper = new ThriftResponseTimeBoMapper();
        return mapper.map(original);
    }

    @Override
    protected void verify(TResponseTime original, ResponseTimeBo mappedStatDataPoint) {
        Assert.assertEquals("avg", original.getAvg(), mappedStatDataPoint.getAvg());
        Assert.assertEquals("max", original.getMax(), mappedStatDataPoint.getMax());
    }

}
