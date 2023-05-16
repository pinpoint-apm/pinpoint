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

package com.navercorp.pinpoint.collector.mapper.flink;

import com.navercorp.pinpoint.common.server.bo.stat.TotalThreadCountBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFTotalThreadCount;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TFTotalThreadCountMapperTest {
    @Test
    public void mapTest() {
        TFTotalThreadCountMapper mapper = new TFTotalThreadCountMapper();
        TotalThreadCountBo totalThreadCountBo = new TotalThreadCountBo();
        totalThreadCountBo.setTotalThreadCount(50);
        TFTotalThreadCount tfTotalThreadCount = mapper.map(totalThreadCountBo);
        assertEquals(tfTotalThreadCount.getTotalThreadCount(), 50);
    }
}
