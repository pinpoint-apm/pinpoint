/*
 * Copyright 2016 Naver Corp.
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

import com.navercorp.pinpoint.collector.mapper.thrift.ThriftBoMapper;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.thrift.dto.TJvmGc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author HyunGil Jeong
 */
@Component
public class ThriftJvmGcBoMapper implements ThriftBoMapper<JvmGcBo, TJvmGc> {

    @Autowired
    private ThriftJvmGcTypeMapper jvmGcTypeMapper;

    @Override
    public JvmGcBo map(TJvmGc jvmGc) {
        JvmGcBo jvmGcBo = new JvmGcBo();
        jvmGcBo.setGcType(this.jvmGcTypeMapper.map(jvmGc.getType()));
        jvmGcBo.setHeapUsed(jvmGc.getJvmMemoryHeapUsed());
        jvmGcBo.setHeapMax(jvmGc.getJvmMemoryHeapMax());
        jvmGcBo.setNonHeapUsed(jvmGc.getJvmMemoryNonHeapUsed());
        jvmGcBo.setNonHeapMax(jvmGc.getJvmMemoryNonHeapMax());
        jvmGcBo.setGcOldCount(jvmGc.getJvmGcOldCount());
        jvmGcBo.setGcOldTime(jvmGc.getJvmGcOldTime());
        return jvmGcBo;
    }
}