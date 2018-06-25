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
import com.navercorp.pinpoint.thrift.dto.flink.TFFileDescriptor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Roy Kim
 */
public class TFFileDescriptorMapperTest {
    @Test
    public void mapTest() throws Exception {
        TFFileDescriptorMapper tFFileDescriptorMapper = new TFFileDescriptorMapper();
        FileDescriptorBo fileDescriptorBo = new FileDescriptorBo();
        fileDescriptorBo.setOpenFileDescriptorCount(30);
        TFFileDescriptor tFFileDescriptor = tFFileDescriptorMapper.map(fileDescriptorBo);
        assertEquals(tFFileDescriptor.getOpenFileDescriptorCount(), 30, 0);
    }

}