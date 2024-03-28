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

package com.navercorp.pinpoint.exceptiontrace.common.model;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.exceptiontrace.common.util.HashUtils;


import java.util.List;
import java.util.Objects;

/**
 * @author intr3p1d
 */
public class ExceptionMetaData {

    private String tenantId;

    private long timestamp;

    private String transactionId;
    private long spanId;
    private long exceptionId;

    private String applicationServiceType;
    private String applicationName;
    private String agentId;
    private String uriTemplate;

    private String errorClassName;
    private String errorMessage;
    private int exceptionDepth;

    private List<StackTraceElementWrapper> stackTrace;

    private String stackTraceHash;

    public ExceptionMetaData() {
    }

    public ExceptionMetaData(
            String tenantId,
            long timestamp,
            String transactionId,
            long spanId,
            long exceptionId,
            String applicationServiceType,
            String applicationName,
            String agentId,
            String uriTemplate,
            String errorClassName,
            String errorMessage,
            int exceptionDepth,
            List<StackTraceElementWrapper> stackTrace,
            String stackTraceHash
    ) {
        this.tenantId = tenantId;
        this.timestamp = timestamp;
        this.transactionId = StringPrecondition.requireHasLength(transactionId, "transactionId");
        this.spanId = spanId;
        this.exceptionId = exceptionId;
        this.applicationServiceType = StringPrecondition.requireHasLength(applicationServiceType, "applicationServiceType");
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
        this.uriTemplate = uriTemplate;
        this.errorClassName = StringPrecondition.requireHasLength(errorClassName, "errorClassName");
        this.errorMessage = Objects.requireNonNull(errorMessage, "errorMessage");
        this.exceptionDepth = exceptionDepth;
        this.stackTrace = stackTrace;
        this.stackTraceHash = stackTraceHash;
    }

    public static ExceptionMetaData valueOf(
            String tenantId,
            long timestamp, String transactionId, long spanId, long exceptionId,
            String applicationServiceType, String applicationName, String agentId,
            String uriTemplate,
            String errorClassName, String errorMessage, int exceptionDepth,
            List<StackTraceElementWrapper> wrappers
    ) {
        return new ExceptionMetaData(
                tenantId,
                timestamp,
                transactionId,
                spanId,
                exceptionId,
                applicationServiceType,
                applicationName,
                agentId,
                uriTemplate,
                errorClassName,
                errorMessage,
                exceptionDepth,
                wrappers,
                HashUtils.objectsToHashString(wrappers, StackTraceElementWrapper.funnel())
        );
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public long getSpanId() {
        return spanId;
    }

    public void setSpanId(long spanId) {
        this.spanId = spanId;
    }

    public long getExceptionId() {
        return exceptionId;
    }

    public void setExceptionId(long exceptionId) {
        this.exceptionId = exceptionId;
    }

    public String getApplicationServiceType() {
        return applicationServiceType;
    }

    public void setApplicationServiceType(String applicationServiceType) {
        this.applicationServiceType = applicationServiceType;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getUriTemplate() {
        return uriTemplate;
    }

    public void setUriTemplate(String uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    public String getErrorClassName() {
        return errorClassName;
    }

    public void setErrorClassName(String errorClassName) {
        this.errorClassName = errorClassName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getExceptionDepth() {
        return exceptionDepth;
    }

    public void setExceptionDepth(int exceptionDepth) {
        this.exceptionDepth = exceptionDepth;
    }

    public List<StackTraceElementWrapper> getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(List<StackTraceElementWrapper> stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getStackTraceHash() {
        return stackTraceHash;
    }

    public void setStackTraceHash(String stackTraceHash) {
        this.stackTraceHash = stackTraceHash;
    }

    @Override
    public String toString() {
        return "ExceptionMetaData{" +
                "tenantId='" + tenantId + '\'' +
                ", timestamp=" + timestamp +
                ", transactionId='" + transactionId + '\'' +
                ", spanId=" + spanId +
                ", exceptionId=" + exceptionId +
                ", applicationServiceType='" + applicationServiceType + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", uriTemplate='" + uriTemplate + '\'' +
                ", errorClassName='" + errorClassName + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", exceptionDepth=" + exceptionDepth +
                ", stackTrace=" + stackTrace +
                ", stackTraceHash='" + stackTraceHash + '\'' +
                '}';
    }
}
