/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.stat.DirectBufferBo;
import com.navercorp.pinpoint.thrift.dto.TDirectBuffer;
import org.junit.Assert;

/**
 * @author Roy Kim
 */
public class ThriftDirectBufferBoMapperTest extends ThriftBoMapperTestBase<TDirectBuffer, DirectBufferBo> {

    @Override
    protected TDirectBuffer create() {
        TDirectBuffer directBuffer = new TDirectBuffer();
        directBuffer.setDirectCount(getRandomLong(0, 5000));
        directBuffer.setDirectMemoryUsed(getRandomLong(0, 1000000));
        directBuffer.setMappedCount(getRandomLong(0, 5000));
        directBuffer.setMappedMemoryUsed(getRandomLong(0, 1000000));
        return directBuffer;
    }

    @Override
    protected DirectBufferBo convert(TDirectBuffer original) {
        ThriftDirectBufferBoMapper directBufferBoMapper = new ThriftDirectBufferBoMapper();
        return directBufferBoMapper.map(original);
    }

    @Override
    protected void verify(TDirectBuffer original, DirectBufferBo mappedStatDataPoint) {
        Assert.assertEquals("DirectCount", original.getDirectCount(), mappedStatDataPoint.getDirectCount(), 0);
        Assert.assertEquals("DirectMemoryUsed", original.getDirectMemoryUsed(), mappedStatDataPoint.getDirectMemoryUsed(), 0);
        Assert.assertEquals("MappedCount", original.getMappedCount(), mappedStatDataPoint.getMappedCount(), 0);
        Assert.assertEquals("MappedMemoryUsed", original.getMappedMemoryUsed(), mappedStatDataPoint.getMappedMemoryUsed(), 0);
    }

}
