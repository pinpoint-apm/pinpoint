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

import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.thrift.dto.TCpuLoad;
import org.junit.jupiter.api.Assertions;

/**
 * @author Taejin Koo
 */
public class ThriftCpuLoadBoMapperTest extends ThriftBoMapperTestBase<TCpuLoad, CpuLoadBo> {

    @Override
    protected TCpuLoad create() {
        TCpuLoad cpuLoad = new TCpuLoad();
        cpuLoad.setJvmCpuLoad(getRandomDouble(0, 100));
        cpuLoad.setSystemCpuLoad(getRandomDouble(0, 100));
        return cpuLoad;
    }

    @Override
    protected CpuLoadBo convert(TCpuLoad original) {
        ThriftCpuLoadBoMapper cpuLoadBoMapper = new ThriftCpuLoadBoMapper();
        return cpuLoadBoMapper.map(original);
    }

    @Override
    protected void verify(TCpuLoad original, CpuLoadBo mappedStatDataPoint) {
        Assertions.assertEquals(original.getJvmCpuLoad(), mappedStatDataPoint.getJvmCpuLoad(), 0, "jvmCpuLoad");
        Assertions.assertEquals(original.getSystemCpuLoad(), mappedStatDataPoint.getSystemCpuLoad(), 0, "systemCpuLoad");
    }

}
