/*
 * Copyright 2024 NAVER Corp.
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

import com.navercorp.pinpoint.exceptiontrace.web.model.params.GroupFilterParams;
import com.navercorp.pinpoint.exceptiontrace.web.model.params.TransactionSearchParams;

import java.util.List;

/**
 * @author intr3p1d
 */
public class ErrorSummary implements Grouped {
    private GroupedFieldName groupedFieldName;
    private GroupFilterParams groupFilterParams;
    private List<Integer> values;
    private long count;

    private String firstLineOfClassName;
    private String firstLineOfMethodName;

    private long lastTimestamp;

    private TransactionSearchParams lastTransactionSearchParams;


    public ErrorSummary() {
    }

    @Override
    public GroupedFieldName getGroupedFieldName() {
        return groupedFieldName;
    }

    @Override
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

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public TransactionSearchParams getLastTransactionSearchParams() {
        return lastTransactionSearchParams;
    }

    public void setLastTransactionSearchParams(TransactionSearchParams lastTransactionSearchParams) {
        this.lastTransactionSearchParams = lastTransactionSearchParams;
    }
}
