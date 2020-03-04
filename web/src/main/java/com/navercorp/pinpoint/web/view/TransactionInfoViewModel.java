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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.util.DateUtils;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.calltree.span.TraceState;
import com.navercorp.pinpoint.web.config.LogConfiguration;
import com.navercorp.pinpoint.web.vo.callstacks.Record;
import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author jaehong.kim
 * @author minwoo.jung
 */
public class TransactionInfoViewModel {

    private TransactionId transactionId;
    private long spanId;
    private Collection<Node> nodes;
    private Collection<Link> links;
    private RecordSet recordSet;
    private TraceState.State completeState;

    private LogConfiguration logConfiguration;

    public TransactionInfoViewModel(TransactionId transactionId, long spanId, Collection<Node> nodes, Collection<Link> links, RecordSet recordSet, TraceState.State state, LogConfiguration logConfiguration) {
        this.transactionId = transactionId;
        this.spanId = spanId;
        this.nodes = nodes;
        this.links = links;
        this.recordSet = recordSet;
        this.completeState = state;
        this.logConfiguration = Objects.requireNonNull(logConfiguration, "logConfiguration");
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

    @JsonProperty("logLinkEnable")
    public boolean isLogLinkEnable() {
        return logConfiguration.isLogLinkEnable();
    }

    @JsonProperty("loggingTransactionInfo")
    public boolean isLoggingTransactionInfo() {
        return recordSet.isLoggingTransactionInfo();
    }

    @JsonProperty("logButtonName")
    public String getLogButtonName() {
        return logConfiguration.getLogButtonName();
    }

    @JsonProperty("logPageUrl")
    public String getLogPageUrl() {
        final String logPageUrl = logConfiguration.getLogPageUrl();
        if (StringUtils.isNotEmpty(logPageUrl)) {
            StringBuilder sb = new StringBuilder();
            sb.append("transactionId=").append(getTransactionId());
            sb.append("&spanId=").append(spanId);
            sb.append("&applicationName=").append(getApplicationId());
            sb.append("&time=").append(recordSet.getStartTime());
            return logPageUrl + "?" + sb.toString();
        }

        return "";
    }

    @JsonProperty("disableButtonMessage")
    public String getDisableButtonMessage() {
        return logConfiguration.getDisableButtonMessage();
    }

    @JsonProperty("callStackIndex")
    public Map<String, Integer> getCallStackIndex() {
        final Map<String, Integer> index = new HashMap<String, Integer>();
        for (int i = 0; i < CallStack.INDEX.length; i++) {
            index.put(CallStack.INDEX[i], i);
        }

        return index;
    }

    @JsonProperty("callStack")
    public List<CallStack> getCallStack() {

        List<CallStack> list = new ArrayList<CallStack>();
        boolean first = true;
        long barRatio = 0;
        for(Record record : recordSet.getRecordList()) {
            if(first) {
                if(record.isMethod()) {
                    long begin = record.getBegin();
                    long end = record.getBegin() + record.getElapsed();
                    if(end  - begin > 0) {
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
    public Map<String, List<Object>> getApplicationMapData() {
        Map<String, List<Object>> result = new HashMap<String, List<Object>>();

        List<Object> nodeDataArray = new ArrayList<>(nodes);
        result.put("nodeDataArray", nodeDataArray);

        List<Object> linkDataArray = new ArrayList<>(links);
        result.put("linkDataArray", linkDataArray);

        return result;
    }

    @JsonSerialize(using=TransactionInfoCallStackSerializer.class)
    public static class CallStack {
        static final String[] INDEX = {"depth",
                "begin",
                "end",
                "excludeFromTimeline",
                "applicationName",
                "tab",
                "id",
                "parentId",
                "isMethod",
                "hasChild",
                "title",
                "arguments",
                "executeTime",
                "gap",
                "elapsedTime",
                "barWidth",
                "executionMilliseconds",
                "simpleClassName",
                "methodType",
                "apiType",
                "agent",
                "isFocused",
                "hasException",
                "isAuthorized"
        };

        private String depth = "";
        private long begin;
        private long end;
        private boolean excludeFromTimeline;
        private String applicationName = "";
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
        private boolean isFocused;
        private boolean hasException;
        private boolean isAuthorized;

        public CallStack(final Record record, long barRatio) {
            begin = record.getBegin();
            end = record.getBegin() + record.getElapsed();
            excludeFromTimeline = record.isExcludeFromTimeline();
            applicationName = record.getApplicationName();
            tab = record.getTab();
            id = String.valueOf(record.getId());
            if (record.getParentId() > 0) {
                parentId = String.valueOf(record.getParentId());
            }
            isMethod = record.isMethod();
            hasChild = record.getHasChild();
            title = StringEscapeUtils.escapeJson(record.getTitle());
            //arguments = StringEscapeUtils.escapeJson(StringEscapeUtils.escapeHtml4(record.getArguments()));
            arguments = record.getArguments();
            if (record.isMethod()) {
                executeTime = DateUtils.longToDateStr(record.getBegin(), "HH:mm:ss SSS"); // time format
                gap = String.valueOf(record.getGap());
                elapsedTime = String.valueOf(record.getElapsed());
                barWidth = String.format("%1d", (int)(((end - begin) * barRatio) + 0.9));
                executionMilliseconds = String.valueOf(record.getExecutionMilliseconds());
            }
            simpleClassName = record.getSimpleClassName();
            methodType = String.valueOf(record.getMethodTypeEnum().getCode());
            apiType = record.getApiType();
            agent = record.getAgent();
            isFocused = record.isFocused();
            hasException = record.getHasException();
            isAuthorized = record.isAuthorized();
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

        public boolean isFocused() {
            return isFocused;
        }

        public boolean isHasException() {
            return hasException;
        }
        
        public boolean isAuthorized() {
            return isAuthorized;
        }
    }
}