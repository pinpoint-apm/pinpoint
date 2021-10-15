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

package com.navercorp.pinpoint.collector.mapper.flink;

import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFCpuLoad;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author minwoo.jung
 */
public class TFCpuLoadMapperTest {
    @Test
    public void mapTest() {
        TFCpuLoadMapper tFCpuLoadMapper = new TFCpuLoadMapper();
        CpuLoadBo cpuLoadBo = new CpuLoadBo();
        cpuLoadBo.setJvmCpuLoad(30);
        cpuLoadBo.setSystemCpuLoad(50);
        TFCpuLoad tFCpuLoad = tFCpuLoadMapper.map(cpuLoadBo);
        assertEquals(tFCpuLoad.getJvmCpuLoad(), 30, 0);
        assertEquals(tFCpuLoad.getSystemCpuLoad(), 50, 0);
    }

}