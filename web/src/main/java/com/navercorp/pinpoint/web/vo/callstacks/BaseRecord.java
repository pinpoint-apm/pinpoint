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
public abstract class BaseRecord implements Record {
    protected int tab;
    protected int id;
    protected int parentId;
    protected boolean method;
    protected String title;
    protected String arguments;
    protected long begin;
    protected long elapsed;
    protected long gap;
    protected String agentId;
    protected String agentName;
    protected String applicationName;
    protected ServiceType serviceType;
    protected String destinationId;
    protected boolean hasChild;
    protected boolean hasException;
    protected String transactionId;
    protected long spanId;
    protected long executionMilliseconds;
    protected MethodTypeEnum methodTypeEnum = MethodTypeEnum.DEFAULT;
    protected boolean isAuthorized;

    protected boolean excludeFromTimeline;
    protected boolean focused;
    protected String simpleClassName = "";
    protected String fullApiDescription = "";

    protected int lineNumber = 0;
    protected String location = "";

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

    @Override
    public String getAgentName() {
        return agentName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getApiType() {
        if (destinationId == null) {
            if (serviceType == null) {
                // no ServiceType when parameter
                return "";
            }
            return serviceType.getDesc();
        }
        if (serviceType.isIncludeDestinationId()) {
            return serviceType.getDesc() + "(" + destinationId + ")";
        } else {
            return serviceType.getDesc();
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
                ", agentName=" +
                agentName +
                ", applicationName=" +
                applicationName +
                ", serviceType=" +
                serviceType +
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
                "}";
    }
}