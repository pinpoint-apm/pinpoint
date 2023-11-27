/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author jaehong.kim
 */
public class DefaultRecord extends BaseRecord {
    public DefaultRecord(int tab, int id, int parentId, boolean method, String title, String arguments,
                         long begin, long elapsed, long gap, String agentId, String agentName, String applicationName, ServiceType serviceType,
                         String destinationId, boolean hasChild, boolean hasException, String transactionId, long spanId,
                         long executionMilliseconds, MethodTypeEnum methodTypeEnum, boolean isAuthorized, int lineNumber, String location) {
        this.tab = tab;
        this.id = id;
        this.parentId = parentId;
        this.method = method;

        this.title = title;
        this.arguments = arguments;
        this.begin = begin;
        this.elapsed = elapsed;
        this.gap = gap;
        this.agentId = agentId;
        this.agentName = agentName;

        this.applicationName = applicationName;
        this.apiServiceType = serviceType;
        this.destinationId = destinationId;

        this.excludeFromTimeline = serviceType == null || serviceType.isInternalMethod();
        this.hasChild = hasChild;
        this.hasException = hasException;

        this.transactionId = transactionId;
        this.spanId = spanId;

        this.executionMilliseconds = executionMilliseconds;
        this.methodTypeEnum = methodTypeEnum;
        this.isAuthorized = isAuthorized;

        this.lineNumber = lineNumber;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public int getParentId() {
        return parentId;
    }

    public int getTab() {
        return tab;
    }

    public String getTabspace() {
        if (tab == 0) {
            return "";
        }
        return "&nbsp".repeat(Math.max(0, tab));
    }

    public boolean isMethod() {
        return method;
    }

    public String getTitle() {
        return title;
    }

    public String getArguments() {
        return arguments;
    }

    public long getBegin() {
        return begin;
    }

    public long getElapsed() {
        return elapsed;
    }

    public long getGap() {
        return gap;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getApiType() {
        if (destinationId == null) {
            if (apiServiceType == null) {
                // no ServiceType when parameter
                return "";
            }
            return apiServiceType.getDesc();
        }
        if (apiServiceType.isIncludeDestinationId()) {
            return apiServiceType.getDesc() + "(" + destinationId + ")";
        } else {
            return apiServiceType.getDesc();
        }

    }

    public boolean isExcludeFromTimeline() {
        return excludeFromTimeline;
    }

    public String getSimpleClassName() {
        return simpleClassName;
    }

    public void setSimpleClassName(String simpleClassName) {
        this.simpleClassName = simpleClassName;
    }

    public String getFullApiDescription() {
        return fullApiDescription;
    }

    public void setFullApiDescription(String fullApiDescription) {
        this.fullApiDescription = fullApiDescription;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean getHasChild() {
        return hasChild;
    }

    public boolean getHasException() {
        return hasException;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public long getSpanId() {
        return spanId;
    }

    public long getExecutionMilliseconds() {
        return executionMilliseconds;
    }

    public MethodTypeEnum getMethodTypeEnum() {
        return methodTypeEnum;
    }

    public boolean isAuthorized() {
        return this.isAuthorized;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "{tab=" +
                tab +
                ", id=" +
                id +
                ", parentId=" +
                parentId +
                ", method=" +
                method +
                ", title=" +
                title +
                ", simpleClassName=" +
                simpleClassName +
                ", fullApiDescription=" +
                fullApiDescription +
                ", arguments=" +
                arguments +
                ", begin=" +
                begin +
                ", elapsed=" +
                elapsed +
                ", gap=" +
                gap +
                ", executionMilliseconds=" +
                executionMilliseconds +
                ", agentId=" +
                agentId +
                ", applicationName=" +
                applicationName +
                ", serviceType=" +
                apiServiceType +
                ", destinationId=" +
                destinationId +
                ", excludeFromTimeline=" +
                excludeFromTimeline +
                ", transactionId=" +
                transactionId +
                ", spanId=" +
                spanId +
                ", focused=" +
                focused +
                ", hasChild=" +
                hasChild +
                ", hasException=" +
                hasException +
                ", methodTypeEnum=" +
                methodTypeEnum +
                ", isAuthorized=" +
                isAuthorized +
                ", lineNumber=" +
                lineNumber +
                ", location=" +
                location +
                "}";
    }
}
