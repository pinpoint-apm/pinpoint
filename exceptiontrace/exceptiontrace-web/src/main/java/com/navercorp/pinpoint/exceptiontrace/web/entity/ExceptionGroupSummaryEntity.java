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
package com.navercorp.pinpoint.exceptiontrace.web.entity;

/**
 * @author intr3p1d
 */
public class ExceptionGroupSummaryEntity extends GroupedFieldNameEntity {
    private String values;
    private long count;
    private String mostRecentErrorClass;
    private String mostRecentErrorMessage;
    private String firstLineOfClassName;
    private String firstLineOfMethodName;
    private long firstOccurred;
    private long lastOccurred;
    private String applicationName;
    private String agentId;
    private String transactionId;
    private String spanId;
    private String exceptionId;

    public ExceptionGroupSummaryEntity() {
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getMostRecentErrorClass() {
        return mostRecentErrorClass;
    }

    public void setMostRecentErrorClass(String mostRecentErrorClass) {
        this.mostRecentErrorClass = mostRecentErrorClass;
    }

    public String getMostRecentErrorMessage() {
        return mostRecentErrorMessage;
    }

    public void setMostRecentErrorMessage(String mostRecentErrorMessage) {
        this.mostRecentErrorMessage = mostRecentErrorMessage;
    }

    public String getFirstLineOfClassName() {
        return firstLineOfClassName;
    }

    public void setFirstLineOfClassName(String firstLineOfClassName) {
        this.firstLineOfClassName = firstLineOfClassName;
    }

    public String getFirstLineOfMethodName() {
        return firstLineOfMethodName;
    }

    public void setFirstLineOfMethodName(String firstLineOfMethodName) {
        this.firstLineOfMethodName = firstLineOfMethodName;
    }

    public long getFirstOccurred() {
        return firstOccurred;
    }

    public void setFirstOccurred(long firstOccurred) {
        this.firstOccurred = firstOccurred;
    }

    public long getLastOccurred() {
        return lastOccurred;
    }

    public void setLastOccurred(long lastOccurred) {
        this.lastOccurred = lastOccurred;
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
}
