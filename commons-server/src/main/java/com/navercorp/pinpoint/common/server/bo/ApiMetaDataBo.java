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

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.server.util.RowKeyUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;

import java.util.Objects;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class ApiMetaDataBo {
    private String agentId;
    private long startTime;

    private int apiId;

    private String apiInfo;
    private int lineNumber = -1;
    private MethodTypeEnum methodTypeEnum = MethodTypeEnum.DEFAULT;

    public ApiMetaDataBo() {
    }

    public ApiMetaDataBo(String agentId, long startTime, int apiId) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.startTime = startTime;
        this.apiId = apiId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }


    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getApiInfo() {
        return apiInfo;
    }

    public void setApiInfo(String apiInfo) {
        this.apiInfo = apiInfo;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public MethodTypeEnum getMethodTypeEnum() {
        return methodTypeEnum;
    }

    public void setMethodTypeEnum(MethodTypeEnum methodTypeEnum) {
        this.methodTypeEnum = Objects.requireNonNull(methodTypeEnum, "methodTypeEnum");
    }
    
    public String getDescription() {
        if (lineNumber != -1) {
            return apiInfo + ":" + lineNumber;
        }
        
        return apiInfo;
    }

    public void readRowKey(byte[] bytes) {
        this.agentId = BytesUtils.safeTrim(BytesUtils.toString(bytes, 0, PinpointConstants.AGENT_NAME_MAX_LEN));
        this.startTime = TimeUtils.recoveryTimeMillis(readTime(bytes));
        this.apiId = readKeyCode(bytes);
    }

    private static long readTime(byte[] rowKey) {
        return BytesUtils.bytesToLong(rowKey, PinpointConstants.AGENT_NAME_MAX_LEN);
    }

    private static int readKeyCode(byte[] rowKey) {
        return BytesUtils.bytesToInt(rowKey, PinpointConstants.AGENT_NAME_MAX_LEN + BytesUtils.LONG_BYTE_LENGTH);
    }

    public byte[] toRowKey() {
        return RowKeyUtils.getMetaInfoRowKey(this.agentId, this.startTime, this.apiId);
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