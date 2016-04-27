/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.collector.mapper.thrift;

import com.navercorp.pinpoint.common.server.bo.JvmInfoBo;
import com.navercorp.pinpoint.thrift.dto.TJvmInfo;
import org.springframework.stereotype.Component;

/**
 * @author HyunGil Jeong
 */
@Component
public class JvmInfoBoMapper implements ThriftBoMapper<JvmInfoBo, TJvmInfo> {
    @Override
    public JvmInfoBo map(TJvmInfo thriftObject) {
        short version = thriftObject.getVersion();
        String jvmVersion = thriftObject.getVmVersion();
        String gcTypeName = thriftObject.getGcType().name();
        JvmInfoBo jvmInfoBo = new JvmInfoBo(version);
        jvmInfoBo.setJvmVersion(jvmVersion);
        jvmInfoBo.setGcTypeName(gcTypeName);
        return jvmInfoBo;
    }
}
