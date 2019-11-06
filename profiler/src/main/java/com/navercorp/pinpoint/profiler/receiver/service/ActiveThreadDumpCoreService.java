/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.receiver.service;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.ThreadMXBeanUtils;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceSnapshot;


import java.lang.management.ThreadInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ActiveThreadDumpCoreService {

    private final ActiveTraceRepository activeTraceRepository;

    private final Comparator<ThreadDump> reverseOrder =  Collections.reverseOrder(new ThreadDumpComparator());

    public ActiveThreadDumpCoreService(ActiveTraceRepository activeTraceRepository) {
        this.activeTraceRepository = Assert.requireNonNull(activeTraceRepository, "activeTraceRepository");
    }

    public Collection<ThreadDump> getActiveThreadDumpList(ThreadDumpRequest request) {

        final List<ActiveTraceSnapshot> activeTraceInfoList = activeTraceRepository.snapshot();

        return getActiveThreadDumpList(activeTraceInfoList, request);
    }

    private Collection<ThreadDump> getActiveThreadDumpList(List<ActiveTraceSnapshot> activeTraceInfoList, ThreadDumpRequest request) {

        if (request.isEnableFilter()) {
            return filterActiveThreadDump(activeTraceInfoList, request);
        } else {

            return getAllActiveThreadDump(activeTraceInfoList, request);
        }
    }

    private Collection<ThreadDump> filterActiveThreadDump(List<ActiveTraceSnapshot> activeTraceInfoList, ThreadDumpRequest request) {

        final Collection<ThreadDump> result = new LimitedList<ThreadDump>(request.getLimit(), reverseOrder);

        for (ActiveTraceSnapshot activeTraceInfo : activeTraceInfoList) {
            final long threadId = activeTraceInfo.getThreadId();
            if (!isTraceThread(threadId)) {
                continue;
            }

            final ThreadDump threadDump = filter(activeTraceInfo, request);
            if (threadDump != null) {
                result.add(threadDump);
            }
        }

        return result;
    }

    private ThreadDump filter(ActiveTraceSnapshot activeTraceInfo, ThreadDumpRequest request) {

        if (request.isEnableLocalTransactionIdFilter()) {

            final long localTransactionId = activeTraceInfo.getLocalTransactionId();
            if (request.findLocalTransactionId(localTransactionId)) {

                final long threadId = activeTraceInfo.getThreadId();
                final ThreadInfo threadInfo = getThreadInfo(threadId, request.getStackTrace());
                if (threadInfo != null) {
                    return newThreadDump(activeTraceInfo, threadInfo);
                }
            }
        }

        if (request.isEnableThreadNameFilter()) {
            // native call
            final long threadId = activeTraceInfo.getThreadId();
            final ThreadInfo threadInfo = getThreadInfo(threadId, request.getStackTrace());
            if (threadInfo != null) {
                if (request.findThreadName(threadInfo.getThreadName())) {
                    return newThreadDump(activeTraceInfo, threadInfo);
                }
            }
        }

        return null;
    }

    private Collection<ThreadDump> getAllActiveThreadDump(List<ActiveTraceSnapshot> activeTraceInfoList, ThreadDumpRequest request) {
        Collection<ThreadDump> activeThreadDumpList = new LimitedList<ThreadDump>(request.getLimit(), reverseOrder);

        for (ActiveTraceSnapshot activeTraceInfo : activeTraceInfoList) {
            final long threadId = activeTraceInfo.getThreadId();
            if (!isTraceThread(threadId)) {
                continue;
            }
            final ThreadInfo threadInfo = getThreadInfo(threadId, request.getStackTrace());
            if (threadInfo != null) {
                ThreadDump threadDump = newThreadDump(activeTraceInfo, threadInfo);
                activeThreadDumpList.add(threadDump);
            }
        }

        return activeThreadDumpList;
    }

    private boolean isTraceThread(long threadId) {
        if (threadId == -1) {
            return false;
        }
        return true;
    }



    private ThreadInfo getThreadInfo(long threadId, StackTrace dumpType) {
        if (threadId == -1) {
            return null;
        }

        if (StackTrace.DUMP == dumpType) {
            return ThreadMXBeanUtils.getThreadInfo(threadId);
        } else {
            return ThreadMXBeanUtils.getThreadInfo(threadId, 0);
        }
    }

    private ThreadDump newThreadDump(ActiveTraceSnapshot activeTraceInfo, ThreadInfo threadInfo) {
        return new ThreadDump(activeTraceInfo, threadInfo);
    }
}
