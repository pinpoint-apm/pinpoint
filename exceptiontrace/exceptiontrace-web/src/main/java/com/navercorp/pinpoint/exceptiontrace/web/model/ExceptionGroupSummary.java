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
package com.navercorp.pinpoint.exceptiontrace.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.exceptiontrace.web.model.params.GroupFilterParams;
import com.navercorp.pinpoint.exceptiontrace.web.model.params.TransactionSearchParams;

import java.util.List;

/**
 * @author intr3p1d
 */
public class ExceptionGroupSummary implements Grouped {

    private List<Integer> values;
    private long count;

    private GroupedFieldName groupedFieldName;
    private GroupFilterParams groupFilterParams;

    private String mostRecentErrorClass;
    private String mostRecentErrorMessage;
    private String firstLineOfClassName;
    private String firstLineOfMethodName;
    private long firstOccurred;
    private long lastOccurred;

    private TransactionSearchParams lastTransactionSearchParams;


    public ExceptionGroupSummary() {
    }

    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        this.values = values;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @JsonProperty("fieldName")
    public GroupedFieldName getGroupedFieldName() {
        return groupedFieldName;
    }

    public void setGroupedFieldName(GroupedFieldName groupedFieldName) {
        this.groupedFieldName = groupedFieldName;
    }

    @Override
    public GroupFilterParams getGroupFilterParams() {
        return groupFilterParams;
    }

    @Override
    public void setGroupFilterParams(GroupFilterParams groupFilterParams) {
        this.groupFilterParams = groupFilterParams;
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

    public TransactionSearchParams getLastTransactionSearchParams() {
        return lastTransactionSearchParams;
    }

    public void setLastTransactionSearchParams(TransactionSearchParams lastTransactionSearchParams) {
        this.lastTransactionSearchParams = lastTransactionSearchParams;
    }

    @Override
    public String toString() {
        return "ExceptionGroupSummary{" +
                "values=" + values +
                ", count=" + count +
                ", groupedFieldName=" + groupedFieldName +
                ", groupFilterParams=" + groupFilterParams +
                ", mostRecentErrorClass='" + mostRecentErrorClass + '\'' +
                ", mostRecentErrorMessage='" + mostRecentErrorMessage + '\'' +
                ", firstOccurred=" + firstOccurred +
                ", lastOccurred=" + lastOccurred +
                ", lastTransactionSearchParams=" + lastTransactionSearchParams +
                '}';
    }
}
