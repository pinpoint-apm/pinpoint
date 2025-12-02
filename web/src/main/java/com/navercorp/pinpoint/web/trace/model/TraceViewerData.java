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
package com.navercorp.pinpoint.web.trace.model;

import com.navercorp.pinpoint.web.trace.callstacks.Record;
import com.navercorp.pinpoint.web.trace.callstacks.RecordSet;
import org.apache.commons.lang3.Strings;
import org.eclipse.collections.api.factory.Stacks;
import org.eclipse.collections.api.factory.primitive.IntIntMaps;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.stack.MutableStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TraceViewerData {
    private static final int START_TIME_INDEX = 0;
    private static final int END_TIME_INDEX = 1;

    private static final int ID_NOT_EXIST = -1;

    private static final int NOT_FOUND = -1;

    private final RecordSet recordSet;
    private final List<TraceEvent> traceEvents;
    private final List<Long[]> occupiedRange;
    private final MutableIntIntMap invisibleRecords;
    private final long minBlank;
    private int maxTid;

    public TraceViewerData(RecordSet recordSet) {
        this.recordSet = recordSet;
        this.maxTid = 0;
        this.traceEvents = new ArrayList<>();
        this.occupiedRange = new ArrayList<>();
        this.invisibleRecords = IntIntMaps.mutable.of();
        this.minBlank = (recordSet.getEndTime() - recordSet.getStartTime()) / 100;
        initialize();
    }

    public List<TraceEvent> getTraceEvents() {
        return traceEvents;
    }

    private int getNewTid(Record record, int tid) {
        long startTime = record.getBegin();
        long endTime = record.getBegin() + record.getElapsed();

        for (int i = tid; i <= maxTid; i++) {
            if ((endTime + minBlank < occupiedRange.get(i)[START_TIME_INDEX]) || (occupiedRange.get(i)[END_TIME_INDEX]) + minBlank < startTime) {
                return i;
            }
        }
        maxTid++;
        return maxTid;
    }

    public void initialize() {
        int tid = 0;
        int arrowId = 0;
        String previousAppName = "";

        Record prev = null;
        Record possibleException = null;

        List<MutableStack<Record>> recordTraces = new ArrayList<>();

        for (Record record : recordSet.getRecordList()) {
            if (record.getElapsed() != 0) {
                boolean isRecordHighlighted = Strings.CS.equals(recordSet.getApplicationName(), record.getApplicationName());
                boolean isApplicationNameChanged = !previousAppName.equals(record.getApplicationName());

                if (recordTraces.isEmpty()) {
                    MutableStack<Record> recordTrace = Stacks.mutable.of();
                    recordTrace.push(record);

                    recordTraces.add(recordTrace);
                    occupiedRange.add(new Long[]{record.getBegin(), record.getBegin() + record.getElapsed()});
                } else if (record.getApiType().equals("ASYNC")) {
                    tid = newAsyncStack(tid, recordTraces, arrowId, record);
                    arrowId++;
                } else if (isApplicationNameChanged && !isContinuingParentStack(recordTraces, tid, record)) {
                    tid = newAsyncStack(tid, recordTraces, arrowId, record);
                    arrowId++;
                } else {
                    tid = updateRecordTrace(recordTraces, tid, record);
                }

                TraceEvent newTrace = TraceEvent.defaultTrace(tid, isRecordHighlighted, record, isApplicationNameChanged);
                traceEvents.add(newTrace);
                previousAppName = record.getApplicationName();
                prev = record;
            } else {
                if (isQueryTitle(record.getTitle())) {
                    if (prev != null) {
                        addQueryInfo(prev, record);
                    }
                }

                if (record.getHasException()) {
                    if ((possibleException.getId() == record.getParentId())) {
                        addExceptionInfo(tid, prev, possibleException, record);
                    }
                }

                addToInvisibleRecords(record);
            }
            possibleException = record;
        }
    }

    private boolean isQueryTitle(String title) {
        return "SQL".equals(title) || "MONGO-JSON".equals(title);
    }

    private void addToInvisibleRecords(Record record) {
        int nonZeroAncestorId = findAncestorId(record.getParentId());
        int parentId = getParentId(record, nonZeroAncestorId);
        invisibleRecords.put(record.getId(), parentId);
    }

    private int getParentId(Record record, int nonZeroAncestorId) {
        if (nonZeroAncestorId == ID_NOT_EXIST) {
            return record.getParentId();
        }
        return nonZeroAncestorId;
    }

    private int findAncestorId(int parentId) {
        int nonZeroAncestorId;
        do {
            nonZeroAncestorId = invisibleRecords.getIfAbsent(parentId, ID_NOT_EXIST);
            if (nonZeroAncestorId == ID_NOT_EXIST) {
                break;
            }
        } while (invisibleRecords.getIfAbsent(nonZeroAncestorId, ID_NOT_EXIST) != ID_NOT_EXIST);
        return nonZeroAncestorId;
    }

    private void addQueryInfo(Record prev, Record record) {
        if ((prev.getId() == record.getParentId()) && (prev.getElapsed() > 0)) {
            TraceEvent parent = traceEvents.get(traceEvents.size()-1);
            parent.changeToDatabaseTrace();
            parent.addArg("Query", record.getArguments());
        }
    }

    private void addExceptionInfo(int tid, Record prev, Record possibleException, Record record) {
        if (possibleException.getElapsed() > 0) {
            TraceEvent parent = traceEvents.get(traceEvents.size()-1);
            parent.changeToRed();
            parent.addArg("Exception", record.getTitle());
            parent.addArg("Exception Details", record.getArguments());
        } else {
            TraceEvent exception = TraceEvent.exceptionTrace(tid, prev);
            traceEvents.add(exception);
        }
    }

    private int newAsyncStack(int tid, List<MutableStack<Record>> recordTraces, int arrowId, Record record) {
        /* Makes the start point of an asynchronous trace arrow */
        int parentTid = getParentTid(recordTraces, tid, record);
        TraceEvent arrowStart = TraceEvent.arrowStartTrace(parentTid, recordTraces.get(parentTid).peek(), record.getBegin(), arrowId);
        traceEvents.add(arrowStart);

        tid = getNewTid(record, tid);

        /* Makes the end point of an asynchronous trace arrow */
        TraceEvent arrowEnd = TraceEvent.arrowEndTrace(tid, record, arrowId);
        traceEvents.add(arrowEnd);

        /* Adds the start call of an asynchronous call stack */
        MutableStack<Record> recordTrace = Stacks.mutable.of();
        recordTrace.push(record);

        recordTraces.add(recordTrace);

        long recordEndTime = record.getBegin() + record.getElapsed();

        if (occupiedRange.size() <= tid) {
            occupiedRange.add(new Long[]{record.getBegin(), recordEndTime});
        } else {
            Long[] currentOccupiedRange = occupiedRange.get(tid);
            if (currentOccupiedRange[START_TIME_INDEX] >  record.getBegin()) {
                currentOccupiedRange[START_TIME_INDEX] = record.getBegin();
            }
            if (currentOccupiedRange[END_TIME_INDEX] < recordEndTime) {
                currentOccupiedRange[END_TIME_INDEX] = recordEndTime;
            }
        }

        return tid;
    }

    private boolean isContinuingParentStack(List<MutableStack<Record>> recordTraces, int tid, Record record) {
        int parentTid = getParentTid(recordTraces, tid, record);
        return recordTraces.get(parentTid).peek().getApplicationName().equals(record.getApplicationName());
    }

    private int updateRecordTrace(List<MutableStack<Record>> recordTraces, int tid, Record record) {
        int parentTid = getParentTid(recordTraces, tid, record);
        MutableStack<Record> recordTrace = recordTraces.get(parentTid);
        recordTrace.push(record);

        return parentTid;
    }

    private int getParentTid(List<MutableStack<Record>> recordTraces, int tid, Record record) {
        int nextTid = tid;
        int searchingFor = getParentId(record);

        while (nextTid >= 0) {
            MutableStack<Record> recordTrace = recordTraces.get(nextTid);
            final int index = pop(recordTrace, searchingFor);
            if (index != NOT_FOUND) {
                return nextTid;
            }
            nextTid--;
        }
        return tid;
    }

    static int pop(MutableStack<Record> stack, int id) {
        final int index = indexOf(stack, id);
        if (index != NOT_FOUND) {
            stack.pop(index);
        }
        return index;
    }

    static int indexOf(MutableStack<Record> stack, int id) {
        int i = 0;
        for (Record r : stack) {
            if (r.getId() == id) {
                return i;
            }
            i++;
        }
        return NOT_FOUND;
    }

    private int getParentId(Record record) {
        int searchingFor = invisibleRecords.getIfAbsent(record.getParentId(), ID_NOT_EXIST);
        return getParentId(record, searchingFor);
    }

    public enum RecordType {
        TRACE("X"),
        EXCEPTION("I"),
        ARROW_START("s"),
        ARROW_END("f");

        private final String key;

        RecordType(String key) {
            this.key = Objects.requireNonNull(key, "key");
        }

        public String key() {
            return key;
        }
    }

    public static class TraceEvent {
        private static final String PID = "";                  /* process id (not used in timeline, but necessary for trace_viewer spec. */
        private String cat;                             /* category name (Exception, Database, Trace) */
        private int tid;                               /* thread id (used to separate async call stacks) */
        private String id;                               /* thread id (used to separate async call stacks) */
        private long ts;                                /* start time (us) */
        private RecordType ph;                              /* trace viewer record type (I = Exception, X = Trace, s = Arrow start, f = Arrow end) */
        private long dur;                               /* process duration (us) */
        private String s = "p";                         /* scope (only uses "p" = process in timeline ) */
        private String name;                            /* trace name */
        private String cname;                           /* color */
        private Map<String, String> args;               /* other arguments */


        public TraceEvent(String cat, int tid, RecordType ph, String cname, final Record record, boolean showApplicationName) {
            this.cat = cat;
            this.tid = tid;
            this.ph = ph;
            this.cname = cname;
            this.id = "";

            this.ts = record.getBegin() * 1000;
            this.dur = record.getElapsed() * 1000;
            this.name = (showApplicationName? ("[" + record.getApplicationName() + "] "): "") + record.getTitle();
            this.cname = cname;

            this.args = new HashMap<>();
            args.put("id", String.valueOf(record.getId()));
            args.put("parentId", String.valueOf(record.getParentId()));
            args.put("API Type", record.getApiType());
            args.put("Application Name", record.getApplicationName());
        }

        static TraceEvent defaultTrace(int tid, boolean isHighlighted, final Record record, boolean showApplicationName) {
            return new TraceEvent("Trace", tid, RecordType.TRACE, (isHighlighted? "": "grey"), record, showApplicationName);
        }

        static TraceEvent exceptionTrace(int tid, final Record record) {
            return new TraceEvent("Exception", tid, RecordType.EXCEPTION, "terrible", record, false);
        }

        static TraceEvent databaseTrace(int tid, boolean isHighlighted, final Record record, boolean showApplicationName) {
            return new TraceEvent("Database", tid, RecordType.TRACE, (isHighlighted? "": "grey"), record, showApplicationName);
        }

        static TraceEvent arrowStartTrace(int tid, final Record parent, long startTime, int arrowId) {
            TraceEvent event = new TraceEvent("Trace", tid, RecordType.ARROW_START, "", parent, false);
            event.name = "Async Trace";
            event.id = Integer.toString(arrowId);
            event.ts = startTime * 1000;
            return event;
        }

        static TraceEvent arrowEndTrace(int tid, final Record record, int arrowId) {
            TraceEvent event = new TraceEvent("Trace", tid, RecordType.ARROW_END, "", record, false);
            event.name = "Async Trace";
            event.id = Integer.toString(arrowId);
            return event;
        }

        public String getCat() { return cat; }

        public String getPid() { return PID; }

        public long getTs() { return ts; }

        public long getTid() { return tid; }

        public String getId() { return id; }

        public String getPh() {
            return ph.key();
        }

        public long getDur() { return dur; }

        public String getS() { return s; }

        public String getName() { return name; }

        public String getCname() { return cname; }

        public Map<String, String> getargs() { return args; }

        public void addArgs(Map<String, String> args) {
            this.args.putAll(args);
        }

        public void addArg(String key, String value) {
            this.args.put(key, value);
        }

        public void changeToDatabaseTrace() {
            this.cat = "Database";
        }

        public void changeToRed() {
            this.cname = "terrible";
        }
    }
}