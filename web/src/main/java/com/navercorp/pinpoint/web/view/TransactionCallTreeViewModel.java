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
package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.util.DateTimeFormatUtils;
import com.navercorp.pinpoint.web.calltree.span.TraceState;
import com.navercorp.pinpoint.web.vo.callstacks.Record;
import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TransactionCallTreeViewModel {
    private final TransactionId transactionId;
    private final long spanId;
    private final RecordSet recordSet;
    private final TraceState.State completeState;

    private final LogLinkView logLinkView;

    public TransactionCallTreeViewModel(TransactionId transactionId, long spanId, RecordSet recordSet, TraceState.State state, LogLinkView logLinkView) {
        this.transactionId = transactionId;
        this.spanId = spanId;

        this.recordSet = recordSet;
        this.completeState = state;
        this.logLinkView = Objects.requireNonNull(logLinkView, "logLinkView");
    }

    @JsonProperty("uri")
    public String getUri() {
        return recordSet.getUri();
    }

    @JsonProperty("transactionId")
    public String getTransactionId() {
        return transactionId.toString();
    }

    @JsonProperty("spanId")
    public long getSpanId() {
        return spanId;
    }

    @JsonProperty("agentId")
    public String getAgentId() {
        return recordSet.getAgentId();
    }

    @JsonProperty("agentName")
    public String getAgentName() {
        return recordSet.getAgentName();
    }

    @JsonProperty("applicationName")
    public String getApplicationName() {
        return recordSet.getApplicationName();
    }

    @JsonProperty("serviceType")
    public String getServiceType() {
        return recordSet.getServiceType();
    }

    @JsonProperty("callStackStart")
    public long getCallStackStart() {
        return recordSet.getStartTime();
    }

    @JsonProperty("callStackEnd")
    public long getCallStackEnd() {
        return recordSet.getEndTime();
    }

    @JsonProperty("completeState")
    public String getCompleteState() {
        return completeState.toString();
    }

    @JsonProperty("loggingTransactionInfo")
    public boolean isLoggingTransactionInfo() {
        return recordSet.isLoggingTransactionInfo();
    }

    @JsonProperty("focusCallStackId")
    public int getFocusCallStackId() {
        return recordSet.getFocusCallStackId();
    }

    @JsonUnwrapped
    public LogLinkView getLogLink() {
        return logLinkView;
    }

    @JsonProperty("callStackIndex")
    public Map<String, Integer> getCallStackIndex() {
        return Field.getFieldMap();
    }

    @JsonProperty("callStack")
    public List<CallStack> getCallStack() {

        List<Record> recordList = recordSet.getRecordList();

        List<CallStack> list = new ArrayList<>(recordList.size());
        boolean first = true;
        long barRatio = 0;
        for (Record record : recordList) {
            if (first) {
                if (record.isMethod()) {
                    barRatio = getBarRatio(record, barRatio);
                }
                first = false;
            }

            list.add(new CallStack(record, barRatio));
        }

        return list;
    }

    private long getBarRatio(Record record, long barRatio) {
        long begin = record.getBegin();
        long end = record.getBegin() + record.getElapsed();
        if (end - begin > 0) {
            barRatio = 100 / (end - begin);
        }
        return barRatio;
    }

    enum Field {
        depth,
        begin,
        end,
        excludeFromTimeline,
        applicationName,
        tab,
        id,
        parentId,
        isMethod,
        hasChild,
        title,
        arguments,
        executeTime,
        gap,
        elapsedTime,
        barWidth,
        executionMilliseconds,
        simpleClassName,
        methodType,
        apiType,
        agent,
        isFocused,
        hasException,
        isAuthorized,
        agentName,
        lineNumber,
        location,
        applicationServiceType,
        exceptionChainId;

        private static final Map<String, Integer> MAP = Collections.unmodifiableMap(toNameOrdinalMap());


        private static Map<String, Integer> toNameOrdinalMap() {
            final Map<String, Integer> index = new LinkedHashMap<>();
            for (Field field : Field.values()) {
                index.put(field.name(), field.ordinal());
            }
            return index;
        }

        public static Map<String, Integer> getFieldMap() {
            return MAP;
        }
    }

    @JsonSerialize(using = TransactionCallTreeCallStackSerializer.class)
    public static class CallStack {
        @Deprecated
        private final String depth = "";
        private final long begin;
        private final long end;
        private final boolean excludeFromTimeline;
        private final String applicationName;
        private final String applicationServiceType;
        private final int tab;
        private final int id;
        private final Integer parentId;
        private final boolean isMethod;
        private final boolean hasChild;
        private final String title;
        private final String arguments;
        private final String executeTime;
        private final Long gap;
        private final Long elapsedTime;
        private final Integer barWidth;
        private final Long executionMilliseconds;
        private final String simpleClassName;
        private final int methodType;
        private final String apiType;
        private final String agent;
        private final String agentName;
        @Deprecated
        private final boolean isFocused;
        private final boolean hasException;
        private final long exceptionChainId;
        private final boolean isAuthorized;
        private final int lineNumber;
        private final String location;

        public CallStack(final Record record, long barRatio) {
            begin = record.getBegin();
            end = record.getBegin() + record.getElapsed();
            excludeFromTimeline = record.isExcludeFromTimeline();
            applicationName = record.getApplicationName();
            applicationServiceType = record.getApplicationServiceType();
            tab = record.getTab();
            id = record.getId();
            parentId = getParentId(record.getParentId());
            isMethod = record.isMethod();
            hasChild = record.getHasChild();
            title = record.getTitle();
            arguments = record.getArguments();
            if (record.isMethod()) {
                executeTime = DateTimeFormatUtils.formatAbsolute(record.getBegin()); // time format
                gap = record.getGap();
                elapsedTime = record.getElapsed();
                barWidth = getBarWidth(barRatio, elapsedTime);
                executionMilliseconds = record.getExecutionMilliseconds();
            } else {
                executeTime = "";
                gap = null;
                elapsedTime = null;
                barWidth = null;
                executionMilliseconds = null;
            }
            simpleClassName = record.getSimpleClassName();
            methodType = record.getMethodTypeEnum().getCode();
            apiType = record.getApiType();
            agent = record.getAgentId();
            agentName = record.getAgentName();
            isFocused = false;
            hasException = record.getHasException();
            exceptionChainId = record.getExceptionChainId();
            isAuthorized = record.isAuthorized();
            lineNumber = record.getLineNumber();
            location = record.getLocation();
        }

        private @Nullable Integer getParentId(int parentId) {
            if (parentId > 0) {
                return parentId;
            }
            return null;
        }

        private int getBarWidth(long barRatio, long elapsedTime) {
            return (int) ((elapsedTime * barRatio) + 0.9);
        }

        @Deprecated
        public String getDepth() {
            return depth;
        }

        public long getBegin() {
            return begin;
        }

        public long getEnd() {
            return end;
        }

        public boolean isExcludeFromTimeline() {
            return excludeFromTimeline;
        }

        public String getApplicationName() {
            return applicationName;
        }

        public String getApplicationServiceType() {
            return applicationServiceType;
        }

        public int getTab() {
            return tab;
        }

        public int getId() {
            return id;
        }

        public Integer getParentId() {
            return parentId;
        }

        public boolean isMethod() {
            return isMethod;
        }

        public boolean isHasChild() {
            return hasChild;
        }

        public String getTitle() {
            return title;
        }

        public String getArguments() {
            return arguments;
        }

        public String getExecuteTime() {
            return executeTime;
        }

        public Long getGap() {
            return gap;
        }

        public Long getElapsedTime() {
            return elapsedTime;
        }

        public Integer getBarWidth() {
            return barWidth;
        }

        public Long getExecutionMilliseconds() {
            return executionMilliseconds;
        }

        public String getSimpleClassName() {
            return simpleClassName;
        }

        public int getMethodType() {
            return methodType;
        }

        public String getApiType() {
            return apiType;
        }

        public String getAgent() {
            return agent;
        }

        public String getAgentName() {
            return agentName;
        }

        @Deprecated
        public boolean isFocused() {
            return false;
        }

        public boolean isHasException() {
            return hasException;
        }

        public String getExceptionChainId() {
            return exceptionChainId  >= 0 ? String.valueOf(exceptionChainId) : "";
        }

        public boolean isAuthorized() {
            return isAuthorized;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public String getLocation() {
            return location;
        }
    }
}
