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

/**
 * @author intr3p1d
 */
public class ExceptionTraceSummary implements Grouped {

    private GroupedFieldName groupedFieldName;
    private RawGroupedFieldName rawFieldName;
    private String mostRecentErrorClass;
    private String mostRecentErrorMessage;
    private long count;
    private long firstOccurred;
    private long lastOccurred;

    public ExceptionTraceSummary() {
    }

    @JsonProperty("fieldName")
    public GroupedFieldName getGroupedFieldName() {
        return groupedFieldName;
    }

    public void setGroupedFieldName(GroupedFieldName groupedFieldName) {
        this.groupedFieldName = groupedFieldName;
    }

    @JsonProperty("rawFieldName")
    public RawGroupedFieldName getRawFieldName() {
        return rawFieldName;
    }

    public void setRawFieldName(RawGroupedFieldName rawFieldName) {
        this.rawFieldName = rawFieldName;
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

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
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

    @Override
    public String toString() {
        return "ExceptionTraceSummary{" +
                "groupedFieldName=" + groupedFieldName +
                ", mostRecentErrorClass='" + mostRecentErrorClass + '\'' +
                ", mostRecentErrorMessage='" + mostRecentErrorMessage + '\'' +
                ", count=" + count +
                ", firstOccurred=" + firstOccurred +
                ", lastOccurred=" + lastOccurred +
                '}';
    }
}
