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

package com.navercorp.pinpoint.web.trace.callstacks;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.List;

/**
 * @author netspider
 * @author emeroad
 */
public class RecordSet {

    private long startTime = -1;
    private long endTime = -1;
    private long callTreeTimelineEnd = -1;

    private List<Record> recordList;
    private String uri;
    private long beginTimestamp;

    private String agentId;
    private String agentName;

    private String applicationName;
    private String serviceName;

    private String serviceType = ServiceType.UNKNOWN.toString();
    
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

    public long getCallTreeTimelineEnd() {
        return callTreeTimelineEnd;
    }

    public void setCallTreeTimelineEnd(long callTreeTimelineEnd) {
        this.callTreeTimelineEnd = callTreeTimelineEnd;
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

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = StringPrecondition.requireHasLength(serviceName, "serviceName");
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

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}
