/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.mapper.grpc;

import com.navercorp.pinpoint.collector.mapper.grpc.stat.GrpcJvmGcTypeMapper;
import com.navercorp.pinpoint.common.server.bo.JvmInfoBo;
import com.navercorp.pinpoint.grpc.trace.PJvmInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Component
public class GrpcJvmInfoBoMapper {

    private final GrpcJvmGcTypeMapper jvmGcTypeMapper;

    public GrpcJvmInfoBoMapper(GrpcJvmGcTypeMapper jvmGcTypeMapper) {
        this.jvmGcTypeMapper = Objects.requireNonNull(jvmGcTypeMapper, "jvmGcTypeMapper");
    }

    public JvmInfoBo map(final PJvmInfo jvmInfo) {
        final short version = (short) jvmInfo.getVersion();
        final String jvmVersion = jvmInfo.getVmVersion();
        final String gcTypeName = this.jvmGcTypeMapper.map(jvmInfo.getGcType()).name();
        final JvmInfoBo jvmInfoBo = new JvmInfoBo(version);
        jvmInfoBo.setJvmVersion(jvmVersion);
        jvmInfoBo.setGcTypeName(gcTypeName);
        return jvmInfoBo;
    }
}