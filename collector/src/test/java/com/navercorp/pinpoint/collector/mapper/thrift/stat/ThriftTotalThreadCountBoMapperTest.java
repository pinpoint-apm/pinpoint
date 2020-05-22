/*
 * Copyright 2020 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.stat.TotalThreadCountBo;
import com.navercorp.pinpoint.thrift.dto.TTotalThreadCount;
import org.junit.Assert;

public class ThriftTotalThreadCountBoMapperTest extends ThriftBoMapperTestBase<TTotalThreadCount, TotalThreadCountBo> {

    private static final int MAX_THREAD_COUNT = 100;

    @Override
    protected TTotalThreadCount create() {
        TTotalThreadCount tTotalThreadCount = new TTotalThreadCount();
        tTotalThreadCount.setTotalThreadCount(getRandomInteger(0, MAX_THREAD_COUNT));
        return tTotalThreadCount;
    }

    @Override
    protected TotalThreadCountBo convert(TTotalThreadCount original) {
        ThriftTotalThreadCountBoMapper thriftTotalThreadCountBoMapper = new ThriftTotalThreadCountBoMapper();
        return thriftTotalThreadCountBoMapper.map(original);
    }

    @Override
    protected void verify(TTotalThreadCount original, TotalThreadCountBo mappedStatDataPoint) {
        Assert.assertEquals("Total Thread Count", original.getTotalThreadCount(), mappedStatDataPoint.getTotalThreadCount());
    }
}
