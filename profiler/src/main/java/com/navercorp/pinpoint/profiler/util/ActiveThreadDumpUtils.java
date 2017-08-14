/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.ThreadMXBeanUtils;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceSnapshot;

import java.lang.management.ThreadInfo;
import java.util.Comparator;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ActiveThreadDumpUtils {

    private ActiveThreadDumpUtils() {
    }

    private static final ActiveTraceInfoComparator ACTIVE_TRACE_INFO_COMPARATOR = new ActiveTraceInfoComparator();

    public static boolean isTraceThread(ActiveTraceSnapshot activeTraceInfo, List<String> threadNameList, List<Long> traceIdList) {
        final long threadId = activeTraceInfo.getThreadId();
        if (threadId == -1) {
            return false;
        }

        final long localTransactionId = activeTraceInfo.getLocalTransactionId();
        if (findLocalTransactionId(traceIdList, localTransactionId)) {
            return true;
        }


        if (findThreadName(threadNameList, threadId)) {
            return true;
        }

        return false;
    }

    private static boolean findThreadName(List<String> threadNameList, long threadId) {
        if (CollectionUtils.isEmpty(threadNameList)) {
            return false;
        }

        final ThreadInfo thread = ThreadMXBeanUtils.findThread(threadId);
        if (filterThreadName(threadNameList, thread.getThreadName())) {
            return true;
        }
        return false;
    }

    public static boolean findLocalTransactionId(List<Long> traceIdList, long localTransactionId) {
        if (CollectionUtils.isEmpty(traceIdList)) {
            return false;
        }
        if (traceIdList.contains(localTransactionId)) {
            return true;
        }

        return false;
    }

    public static boolean filterThreadName(List<String> threadNameList, String threadName) {
        return threadNameList.contains(threadName);
    }

    public static ActiveTraceInfoComparator getActiveTraceInfoComparator() {
        return ACTIVE_TRACE_INFO_COMPARATOR;
    }

    private static class ActiveTraceInfoComparator implements  Comparator<ActiveTraceSnapshot> {

        private static final int CHANGE_TO_NEW_ELEMENT = 1;
        private static final int KEEP_OLD_ELEMENT = -1;

        @Override
        public int compare(ActiveTraceSnapshot oldElement, ActiveTraceSnapshot newElement) {
            long diff = oldElement.getStartTime() - newElement.getStartTime();

            if (diff <= 0) {
                // Do not change it for the same value for performance.
                return KEEP_OLD_ELEMENT;
            }

            return CHANGE_TO_NEW_ELEMENT;
        }

    }

}
