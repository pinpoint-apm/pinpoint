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
    public DefaultRecord(int tab, int id, int parentId, boolean method, String title, String arguments, long begin, long elapsed, long gap, String agent, String applicationName, ServiceType serviceType, String destinationId, boolean hasChild, boolean hasException, String transactionId, long spanId, long executionMilliseconds, MethodTypeEnum methodTypeEnum, boolean isAuthorized) {
        this.tab = tab;
        this.id = id;
        this.parentId = parentId;
        this.method = method;

        this.title = title;
        this.arguments = arguments;
        this.begin = begin;
        this.elapsed = elapsed;
        this.gap = gap;
        this.agent = agent;

        this.applicationName = applicationName;
        this.serviceType = serviceType;
        this.destinationId = destinationId;

        this.excludeFromTimeline = serviceType == null || serviceType.isInternalMethod();
        this.hasChild = hasChild;
        this.hasException = hasException;

        this.transactionId = transactionId;
        this.spanId = spanId;

        this.executionMilliseconds = executionMilliseconds;
        this.methodTypeEnum = methodTypeEnum;
        this.isAuthorized = isAuthorized;
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
        if(tab == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< tab; i++) {
            sb.append("&nbsp");
        }
        return sb.toString();
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

    public String getAgent() {
        return agent;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{tab=");
        builder.append(tab);
        builder.append(", id=");
        builder.append(id);
        builder.append(", parentId=");
        builder.append(parentId);
        builder.append(", method=");
        builder.append(method);
        builder.append(", title=");
        builder.append(title);
        builder.append(", simpleClassName=");
        builder.append(simpleClassName);
        builder.append(", fullApiDescription=");
        builder.append(fullApiDescription);
        builder.append(", arguments=");
        builder.append(arguments);
        builder.append(", begin=");
        builder.append(begin);
        builder.append(", elapsed=");
        builder.append(elapsed);
        builder.append(", gap=");
        builder.append(gap);
        builder.append(", executionMilliseconds=");
        builder.append(executionMilliseconds);
        builder.append(", agent=");
        builder.append(agent);
        builder.append(", applicationName=");
        builder.append(applicationName);
        builder.append(", serviceType=");
        builder.append(serviceType);
        builder.append(", destinationId=");
        builder.append(destinationId);
        builder.append(", excludeFromTimeline=");
        builder.append(excludeFromTimeline);
        builder.append(", transactionId=");
        builder.append(transactionId);
        builder.append(", spanId=");
        builder.append(spanId);
        builder.append(", focused=");
        builder.append(focused);
        builder.append(", hasChild=");
        builder.append(hasChild);
        builder.append(", hasException=");
        builder.append(hasException);
        builder.append(", methodTypeEnum=");
        builder.append(methodTypeEnum);
        builder.append(", isAuthorized=");
        builder.append(isAuthorized);
        builder.append("}");
        return builder.toString();
    }
}
