/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetaDataRowKey;
import com.navercorp.pinpoint.common.util.LineNumber;

import java.util.Objects;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class ApiMetaDataBo implements MetaDataRowKey {
    private final String agentId;
    private final long startTime;
    private final int apiId;

    private final String apiInfo;
    private final int lineNumber;
    private final MethodTypeEnum methodTypeEnum;

    public ApiMetaDataBo(String agentId, long startTime, int apiId, int lineNumber,
                         MethodTypeEnum methodTypeEnum, String apiInfo) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.startTime = startTime;
        this.apiId = apiId;
        this.lineNumber = lineNumber;
        this.apiInfo = apiInfo;
        this.methodTypeEnum = Objects.requireNonNull(methodTypeEnum, "methodTypeEnum");
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public long getAgentStartTime() {
        return startTime;
    }

    @Override
    public int getId() {
        return apiId;
    }

    public String getApiInfo() {
        return apiInfo;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public MethodTypeEnum getMethodTypeEnum() {
        return methodTypeEnum;
    }

    public String getDescription() {
        if (LineNumber.isLineNumber(lineNumber)) {
            return apiInfo + ":" + lineNumber;
        }
        
        return apiInfo;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ApiMetaDataBo{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", startTime=").append(startTime);
        sb.append(", apiId=").append(apiId);
        sb.append(", apiInfo='").append(apiInfo).append('\'');
        sb.append(", lineNumber=").append(lineNumber);
        sb.append(", methodTypeEnum=").append(methodTypeEnum);
        sb.append('}');
        return sb.toString();
    }
}