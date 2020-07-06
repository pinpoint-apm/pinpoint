/*
 * Copyright 2020 NAVER Corp.
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
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.web.config.LogConfiguration;
import com.navercorp.pinpoint.web.vo.callstacks.Record;
import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Stack;


public class TransactionTimelineInfoViewModel {

    private final TransactionId transactionId;
    private final long spanId;
    private final RecordSet recordSet;
    private List<CallStack> databaseCalls;
    private List<List<CallStack>> rowData;
    private List<List<CallStack>> asyncRowData;
    private List<Boolean> focusedRows;

    private final LogConfiguration logConfiguration;

    public TransactionTimelineInfoViewModel(TransactionId transactionId, long spanId, RecordSet recordSet, LogConfiguration logConfiguration) {
        this.transactionId = transactionId;
        this.spanId = spanId;
        this.recordSet = recordSet;
        this.logConfiguration = Objects.requireNonNull(logConfiguration, "logConfiguration");
        initialize();
    }

    @JsonProperty("transactionId")
    public String getTransactionId() {
        return TransactionIdUtils.formatString(transactionId);
    }

    @JsonProperty("agentId")
    public String getAgentId() { return recordSet.getAgentId(); }

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

    @JsonProperty("barRatio")
    public double getBarRatio() {
        /* total time will take 95% of window width */
        return 95.0 / (recordSet.getEndTime() - recordSet.getStartTime());
    }

    @JsonProperty("databaseCalls")
    public List<CallStack> getDatabaseCalls() {
        return databaseCalls;
    }

    @JsonProperty("callStack")
    public List<List<CallStack>> getCallStack() {
        return rowData;
    }

    @JsonProperty("asyncCallStack")
    public List<List<CallStack>> getAsyncCallStack() { return asyncRowData; }

    @JsonProperty("focusedRows")
    public List<Boolean> getFocusedRows() { return focusedRows;}

    public void initialize() {
        rowData = new ArrayList<List<CallStack>>();
        asyncRowData = new ArrayList<List<CallStack>>();
        databaseCalls = new ArrayList<CallStack>();
        focusedRows = new ArrayList<Boolean>();

        Stack<Integer> asyncRootDepths = new Stack<Integer>();
        Stack<String> asyncRootNames = new Stack<String>();
        Record prev = null;
        boolean isAsync = false;

        for (Record record : recordSet.getRecordList()) {
            if (record.getElapsed() != 0) {
                int depth = record.getTab();
                while (depth >= rowData.size()) {
                    rowData.add(new ArrayList<CallStack>());
                    asyncRowData.add(new ArrayList<CallStack>());
                    focusedRows.add(false);
                }

                isAsync = isCurrentlyAsync(asyncRootDepths, asyncRootNames, prev, record, isAsync);

                CallStack newCall = new CallStack(record);
                if (isAsync) {
                    asyncRowData.get(depth).add(newCall);
                } else {
                    rowData.get(depth).add(newCall);
                    if(newCall.getApplicationName().equals(recordSet.getApplicationId())) {
                        focusedRows.set(depth, true);
                    }
                }
                prev = record;
            } else if ((record.getTitle().equals("SQL") || (record.getTitle().equals("MONGO-JSON")))) {
                if (prev.getId() == record.getParentId()) {
                    databaseCalls.add(new CallStack(prev));
                }
            }
        }

        for (int i = 0; i < rowData.size(); i++) {
            if ((rowData.get(i).size() == 0) && (asyncRowData.get(i).size() == 0)) {
                rowData.remove(i);
                asyncRowData.remove(i);
                focusedRows.remove(i);
            }
        }
    }

    private boolean isCurrentlyAsync(Stack<Integer> asyncRootDepths, Stack<String> asyncRootNames,
                                     Record prev, Record current, boolean isAsync) {
        int asyncInfoLastIndex = asyncRootDepths.size() - 1;
        int currentDepth = current.getTab();

        while ((asyncInfoLastIndex >= 0) && (current.getTab() <= asyncRootDepths.get(asyncInfoLastIndex))) {
            asyncRootDepths.pop();
            asyncRootNames.pop();
            asyncInfoLastIndex--;
            isAsync = false;
        }

        if (asyncInfoLastIndex >= 0) {
            if (current.getApplicationName().equals(asyncRootNames.get(asyncInfoLastIndex))) {
                if (currentDepth > asyncRootDepths.get(asyncInfoLastIndex)) {
                    // returned to previous async trace
                    isAsync = true;
                }
            } else if (!current.getApplicationName().equals(prev.getApplicationName())){
                // now at synchronous application trace
                isAsync = false;
            }
        }

        if (current.getApiType().equals("ASYNC")) {
            isAsync = true;
            asyncRootDepths.push(currentDepth);
            asyncRootNames.push(current.getApplicationName());
        }

        return isAsync;
    }

    public static class CallStack {
        private long begin;
        private long end;
        private String applicationName = "";
        private String methodName = "";
        private int depth;
        private String id = "";
        private String parentId = "";
        private String apiType = "";

        public CallStack(final Record record) {
            begin = record.getBegin();
            end = record.getBegin() + record.getElapsed();
            applicationName = record.getApplicationName();
            methodName = record.getTitle();
            depth = record.getTab();
            id = String.valueOf(record.getId());
            if (record.getParentId() > 0) {
                parentId = String.valueOf(record.getParentId());
            }
            apiType = record.getApiType();
        }

        public int getDepth() {
            return depth;
        }

        public long getBegin() {
            return begin;
        }

        public long getEnd() {
            return end;
        }

        public String getApplicationName() {
            return applicationName;
        }

        public String getId() {
            return id;
        }

        public String getParentId() {
            return parentId;
        }

        public String getApiType() {
            return apiType;
        }

        public String getMethodName() { return methodName; }

    }
}
