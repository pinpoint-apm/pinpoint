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

package com.navercorp.pinpoint.web.vo.callstacks;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;

import java.util.List;

/**
 * @author netspider
 * @author emeroad
 */
public class RecordSet {

    private long startTime = -1;
    private long endTime = -1;

    private List<Record> recordList;
    private String uri;
    private long beginTimestamp;

    private String agentId;
    private String agentName;

    private String applicationName;
    
    private boolean loggingTransactionInfo;
    private int focusCallStackId = -1;

    public RecordSet() {
    }

    public void setRecordList(List<Record> recordList) {
        this.recordList = recordList;
    }

    public List<Record> getRecordList() {
        return recordList;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isStartTimeSet() {
        return startTime != -1;
    }

    public boolean isEndTimeSet() {
        return endTime != -1;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setBeginTimestamp(long beginTimestamp) {
        this.beginTimestamp = beginTimestamp;
    }


    public String getUri() {
        return uri;
    }

    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    /**
     * @deprecated Since 3.1.0. Use {@link #getApplicationName()} instead.
     */
    @Deprecated
    public String getApplicationId() {
        return getApplicationName();
    }

    /**
     * @deprecated Since 3.1.0. Use {@link #setApplicationName(String)} instead.
     */
    @Deprecated
    public void setApplicationId(String applicationId) {
        this.setApplicationName(applicationId);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
    }
    
    public boolean isLoggingTransactionInfo() {
        return loggingTransactionInfo;
    }

    public void setLoggingTransactionInfo(boolean loggingTransactionInfo) {
        this.loggingTransactionInfo = loggingTransactionInfo;
    }

    public int getFocusCallStackId() {
        return focusCallStackId;
    }

    public void setFocusCallStackId(int focusCallStackId) {
        this.focusCallStackId = focusCallStackId;
    }
}
