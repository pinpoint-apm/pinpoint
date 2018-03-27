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
import com.navercorp.pinpoint.thrift.dto.flink.TFDirectBuffer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Roy Kim
 */
public class TFDirectBufferMapperTest {
    @Test
    public void mapTest() throws Exception {
        TFDirectBufferMapper tFDirectBufferMapper = new TFDirectBufferMapper();
        DirectBufferBo directBufferBo = new DirectBufferBo();
        directBufferBo.setDirectCount(30);
        directBufferBo.setDirectMemoryUsed(30);
        directBufferBo.setMappedCount(30);
        directBufferBo.setMappedMemoryUsed(30);
        TFDirectBuffer tFDirectBuffer = tFDirectBufferMapper.map(directBufferBo);
        assertEquals(tFDirectBuffer.getDirectCount(), 30, 0);
        assertEquals(tFDirectBuffer.getDirectMemoryUsed(), 30, 0);
        assertEquals(tFDirectBuffer.getMappedCount(), 30, 0);
        assertEquals(tFDirectBuffer.getMappedMemoryUsed(), 30, 0);
    }

}