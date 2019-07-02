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

import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.thrift.dto.TFileDescriptor;
import org.junit.Assert;

/**
 * @author Roy Kim
 */
public class ThriftFileDescriptorBoMapperTest extends ThriftBoMapperTestBase<TFileDescriptor, FileDescriptorBo> {

    @Override
    protected TFileDescriptor create() {
        TFileDescriptor fileDescriptor = new TFileDescriptor();
        fileDescriptor.setOpenFileDescriptorCount(getRandomLong(0, 1000));
        return fileDescriptor;
    }

    @Override
    protected FileDescriptorBo convert(TFileDescriptor original) {
        ThriftFileDescriptorBoMapper fileDescriptorBoMapper = new ThriftFileDescriptorBoMapper();
        return fileDescriptorBoMapper.map(original);
    }

    @Override
    protected void verify(TFileDescriptor original, FileDescriptorBo mappedStatDataPoint) {
        Assert.assertEquals("openFileDescriptorCount", original.getOpenFileDescriptorCount(), mappedStatDataPoint.getOpenFileDescriptorCount(), 0);
    }

}
