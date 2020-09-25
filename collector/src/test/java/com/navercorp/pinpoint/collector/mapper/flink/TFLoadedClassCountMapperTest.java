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

import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import com.navercorp.pinpoint.thrift.dto.flink.TFLoadedClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TFLoadedClassCountMapperTest {
    @Test
    public void mapTest() {
        TFLoadedClassMapper mapper = new TFLoadedClassMapper();
        LoadedClassBo loadedClassBo = new LoadedClassBo();
        loadedClassBo.setLoadedClassCount(50);
        loadedClassBo.setUnloadedClassCount(50);
        TFLoadedClass tfLoadedClass = mapper.map(loadedClassBo);
        assertEquals(tfLoadedClass.getLoadedClassCount(), 50);
        assertEquals(tfLoadedClass.getUnloadedClassCount(), 50);
    }
}
