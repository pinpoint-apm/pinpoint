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
package com.navercorp.pinpoint.exceptiontrace.web.view;

import com.navercorp.pinpoint.exceptiontrace.common.model.StackTraceElementWrapper;

import java.util.List;

/**
 * @author intr3p1d
 */
public class ExceptionMetaDataView {

    private long timestamp;

    private String transactionId;
    private String spanId;
    private String exceptionId;

    private String applicationServiceType;
    private String applicationName;
    private String agentId;
    private String uriTemplate;

    private String errorClassName;
    private String errorMessage;
    private int exceptionDepth;

    private List<StackTraceElementWrapper> stackTrace;

    private String stackTraceHash;

    public ExceptionMetaDataView() {
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

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getExceptionId() {
        return exceptionId;
    }

    public void setExceptionId(String exceptionId) {
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
}
