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

import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import com.navercorp.pinpoint.thrift.dto.TLoadedClass;
import org.junit.Assert;

public class ThriftLoadedClassCountBoMapperTest extends ThriftBoMapperTestBase<TLoadedClass, LoadedClassBo> {

    private static final int MAX_THREAD_COUNT = 100;

    @Override
    protected TLoadedClass create() {
        TLoadedClass tLoadedClass = new TLoadedClass();
        tLoadedClass.setLoadedClassCount(getRandomInteger(0, MAX_THREAD_COUNT));
        tLoadedClass.setUnloadedClassCount(getRandomInteger(0, MAX_THREAD_COUNT));
        return tLoadedClass;
    }

    @Override
    protected LoadedClassBo convert(TLoadedClass original) {
        ThriftLoadedClassBoMapper thriftLoadedClassBoMapper = new ThriftLoadedClassBoMapper();
        return thriftLoadedClassBoMapper.map(original);
    }

    @Override
    protected void verify(TLoadedClass original, LoadedClassBo mappedStatDataPoint) {
        Assert.assertEquals("Loaded Class Count", original.getLoadedClassCount(), mappedStatDataPoint.getLoadedClassCount());
        Assert.assertEquals("Unloaded Class Count", original.getUnloadedClassCount(), mappedStatDataPoint.getUnloadedClassCount());
    }
}
