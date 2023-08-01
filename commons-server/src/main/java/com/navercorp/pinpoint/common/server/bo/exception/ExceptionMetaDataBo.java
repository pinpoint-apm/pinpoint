/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.common.server.bo.exception;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;

/**
 * @author intr3p1d
 */
public class ExceptionMetaDataBo {

    private final TransactionId transactionId;
    private final long spanId;

    private final short serviceType;
    @NotBlank(message = "applicationName is required") private final String applicationName;
    @NotBlank(message = "agentId is required")  private final String agentId;

    private final String uriTemplate;

    private List<ExceptionWrapperBo> exceptionWrapperBos;


    public ExceptionMetaDataBo(
            TransactionId transactionId, long spanId,
            short serviceType, String applicationName, String agentId,
            String uriTemplate
    ) {
        this.transactionId = transactionId;
        this.spanId = spanId;
        this.serviceType = serviceType;
        this.applicationName = applicationName;
        this.agentId = agentId;
        this.uriTemplate = uriTemplate;
    }

    public TransactionId getTransactionId() {
        return transactionId;
    }

    public long getSpanId() {
        return spanId;
    }

    public short getServiceType() {
        return serviceType;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getUriTemplate() {
        return uriTemplate;
    }

    public List<ExceptionWrapperBo> getExceptionWrapperBos() {
        return exceptionWrapperBos;
    }

    public void setExceptionWrapperBos(List<ExceptionWrapperBo> exceptionWrapperBos) {
        this.exceptionWrapperBos = exceptionWrapperBos;
    }

    @Override
    public String toString() {
        return "ExceptionMetaDataBo{" +
                "transactionId=" + transactionId +
                ", spanId=" + spanId +
                ", serviceType=" + serviceType +
                ", applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", uriTemplate='" + uriTemplate + '\'' +
                ", exceptionWrapperBos=" + exceptionWrapperBos +
                '}';
    }
}