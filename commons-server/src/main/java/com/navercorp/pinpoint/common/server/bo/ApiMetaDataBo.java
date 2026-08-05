/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetaDataRowKey;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.NumberPrecondition;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.common.util.LineNumber;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class ApiMetaDataBo implements MetaDataRowKey {
    private final ServiceUid serviceUid;

    @NonNull
    private final String agentId;
    private final long startTime;
    private final int apiId;

    private final String apiInfo;
    private final int lineNumber;
    private final MethodTypeEnum methodTypeEnum;
    private String location;

    public ApiMetaDataBo(String agentId, long startTime, int apiId, int lineNumber, MethodTypeEnum methodTypeEnum, String apiInfo) {
        this(ServiceUid.DEFAULT, agentId, startTime, apiId, lineNumber, methodTypeEnum, apiInfo);
    }

    public ApiMetaDataBo(ServiceUid serviceUid, String agentId, long startTime, int apiId, int lineNumber, MethodTypeEnum methodTypeEnum, String apiInfo) {
        this.serviceUid = Objects.requireNonNull(serviceUid, "serviceUid");
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
        this.startTime = NumberPrecondition.requirePositiveOrZero(startTime, "startTime");
        this.apiId = apiId;
        this.lineNumber = lineNumber;
        this.apiInfo = apiInfo;
        this.methodTypeEnum = Objects.requireNonNull(methodTypeEnum, "methodTypeEnum");
        this.location = null;
    }

    @Override
    public ServiceUid getServiceUid() {
        return serviceUid;
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

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        if (LineNumber.isLineNumber(lineNumber)) {
            return apiInfo + ":" + lineNumber;
        }
        
        return apiInfo;
    }


    @Override
    public String toString() {
        return "ApiMetaDataBo{" +
                "agentId='" + agentId + '\'' +
                ", startTime=" + startTime +
                ", apiId=" + apiId +
                ", apiInfo='" + apiInfo + '\'' +
                ", lineNumber=" + lineNumber +
                ", methodTypeEnum=" + methodTypeEnum +
                ", location='" + location + '\'' +
                '}';
    }

    public static class Builder {
        private final String agentId;
        private final long startTime;
        private final int apiId;
        private final int lineNumber;
        private final MethodTypeEnum methodTypeEnum;
        private final String apiInfo;
        private final ServiceUid serviceUid;
        private String location;

        public Builder(ServiceUid serviceUid, String agentId, long startTime, int apiId, int lineNumber, MethodTypeEnum methodTypeEnum, String apiInfo) {
            this.serviceUid = serviceUid;
            this.agentId = agentId;
            this.startTime = startTime;
            this.apiId = apiId;
            this.lineNumber = lineNumber;
            this.methodTypeEnum = methodTypeEnum;
            this.apiInfo = apiInfo;
            this.location = null;
        }

        public Builder(String agentId, long startTime, int apiId, int lineNumber, MethodTypeEnum methodTypeEnum, String apiInfo) {
            this(ServiceUid.DEFAULT, agentId, startTime, apiId, lineNumber, methodTypeEnum, apiInfo);
        }

        public Builder setLocation(String location) {
            if (StringUtils.isEmpty(location)) {
                return this;
            }
            this.location = location;
            return this;
        }

        public ApiMetaDataBo build() {
            ApiMetaDataBo result = new ApiMetaDataBo(this.serviceUid, this.agentId, this.startTime, this.apiId, this.lineNumber, this.methodTypeEnum, this.apiInfo);
            result.location = this.location;
            return result;
        }
    }
}