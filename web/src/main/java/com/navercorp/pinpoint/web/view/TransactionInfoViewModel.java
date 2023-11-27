/*
 * Copyright 2019 NAVER Corp.
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
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.common.server.util.DateTimeFormatUtils;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.calltree.span.TraceState;
import com.navercorp.pinpoint.web.vo.callstacks.Record;
import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author jaehong.kim
 * @author minwoo.jung
 */
public class TransactionInfoViewModel {

    private final TransactionId transactionId;
    private final long spanId;
    private final Collection<Node> nodes;
    private final Collection<Link> links;
    private final RecordSet recordSet;
    private final TraceState.State completeState;

    private final LogLinkView logLinkView;
    private TimeHistogramFormat timeHistogramFormat = TimeHistogramFormat.V1;

    public TransactionInfoViewModel(TransactionId transactionId, long spanId,
                                    Collection<Node> nodes, Collection<Link> links,
                                    RecordSet recordSet, TraceState.State state,
                                    LogLinkView logLinkView) {
        this.transactionId = transactionId;
        this.spanId = spanId;
        if (nodes == null) {
            this.nodes = Collections.EMPTY_LIST;
        } else {
            this.nodes = nodes;
        }
        if (links == null) {
            this.links = Collections.EMPTY_LIST;
        } else {
            this.links = links;
        }
        this.recordSet = recordSet;
        this.completeState = state;
        this.logLinkView = Objects.requireNonNull(logLinkView, "logLinkView");
    }

    public void setTimeHistogramFormat(TimeHistogramFormat timeHistogramFormat) {
        this.timeHistogramFormat = timeHistogramFormat;
    }

    @JsonProperty("applicationName")
    public String getApplicationName() {
        return recordSet.getApplicationName();
    }

    @JsonProperty("transactionId")
    public String getTransactionId() {
        return TransactionIdUtils.formatString(transactionId);
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

    @JsonProperty("applicationId")
    public String getApplicationId() {
        return recordSet.getApplicationId();
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

        List<CallStack> list = new ArrayList<>();
        boolean first = true;
        long barRatio = 0;
        for (Record record : recordSet.getRecordList()) {
            if (first) {
                if (record.isMethod()) {
                    long begin = record.getBegin();
                    long end = record.getBegin() + record.getElapsed();
                    if (end - begin > 0) {
                        barRatio = 100 / (end - begin);
                    }
                }
                first = false;
            }

            list.add(new CallStack(record, barRatio));
        }

        return list;
    }

    @JsonProperty("applicationMapData")
    public Map<String, Collection<?>> getApplicationMapData() {

        if (timeHistogramFormat == TimeHistogramFormat.V2) {
            for (Node node : nodes) {
                node.setTimeHistogramFormat(timeHistogramFormat);
            }
            for (Link link : links) {
                link.setTimeHistogramFormat(timeHistogramFormat);
            }
        }

        return Map.of(
                "nodeDataArray", nodes,
                "linkDataArray", links
        );
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

    @JsonSerialize(using = TransactionInfoCallStackSerializer.class)
    public static class CallStack {
        private String depth = "";
        private long begin;
        private long end;
        private boolean excludeFromTimeline;
        private String applicationName = "";
        private String applicationServiceType = "";
        private int tab;
        private String id = "";
        private String parentId = "";
        private boolean isMethod;
        private boolean hasChild;
        private String title = "";
        private String arguments = "";
        private String executeTime = "";
        private String gap = "";
        private String elapsedTime = "";
        private String barWidth = "";
        private String executionMilliseconds = "";
        private String simpleClassName = "";
        private String methodType = "";
        private String apiType = "";
        private String agent = "";
        private String agentName = "";
        private boolean isFocused;
        private boolean hasException;
        private String exceptionChainId = "";
        private boolean isAuthorized;
        private int lineNumber;
        private String location = "";

        public CallStack(final Record record, long barRatio) {
            begin = record.getBegin();
            end = record.getBegin() + record.getElapsed();
            excludeFromTimeline = record.isExcludeFromTimeline();
            applicationName = record.getApplicationName();
            applicationServiceType = record.getApplicationServiceType();
            tab = record.getTab();
            id = String.valueOf(record.getId());
            if (record.getParentId() > 0) {
                parentId = String.valueOf(record.getParentId());
            }
            isMethod = record.isMethod();
            hasChild = record.getHasChild();
            title = record.getTitle();
            arguments = record.getArguments();
            if (record.isMethod()) {
                executeTime = DateTimeFormatUtils.formatAbsolute(record.getBegin()); // time format
                gap = String.valueOf(record.getGap());
                elapsedTime = String.valueOf(record.getElapsed());
                barWidth = String.format("%1d", (int) (((end - begin) * barRatio) + 0.9));
                executionMilliseconds = String.valueOf(record.getExecutionMilliseconds());
            }
            simpleClassName = record.getSimpleClassName();
            methodType = String.valueOf(record.getMethodTypeEnum().getCode());
            apiType = record.getApiType();
            agent = record.getAgentId();
            agentName = record.getAgentName();
            isFocused = record.isFocused();
            hasException = record.getHasException();
            exceptionChainId = String.valueOf(record.getExceptionChainId());
            isAuthorized = record.isAuthorized();
            lineNumber = record.getLineNumber();
            location = record.getLocation();
        }

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

        public String getId() {
            return id;
        }

        public String getParentId() {
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

        public String getGap() {
            return gap;
        }

        public String getElapsedTime() {
            return elapsedTime;
        }

        public String getBarWidth() {
            return barWidth;
        }

        public String getExecutionMilliseconds() {
            return executionMilliseconds;
        }

        public String getSimpleClassName() {
            return simpleClassName;
        }

        public String getMethodType() {
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

        public boolean isFocused() {
            return isFocused;
        }

        public boolean isHasException() {
            return hasException;
        }

        public String getExceptionChainId() {
            return exceptionChainId;
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
