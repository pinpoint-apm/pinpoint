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

package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.thrift.dto.command.TMonitorInfo;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadState;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author Taejin Koo
 */
public final class ThreadDumpUtils {

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String TAB_SEPARATOR = "    "; // tab to 4 spaces

    public static String createDumpMessage(TThreadDump threadDump) {
        TThreadState threadState = getThreadState(threadDump.getThreadState());

        // set threadName
        StringBuilder message = new StringBuilder("\"" + threadDump.getThreadName() + "\"");

        // set threadId
        String hexStringThreadId = Long.toHexString(threadDump.getThreadId());
        message.append(" Id=0x" + hexStringThreadId);

        // set threadState
        message.append(" " + threadState.name());

        if (!StringUtils.isBlank(threadDump.getLockName())) {
            message.append(" on ").append(threadDump.getLockName());
        }

        if (!StringUtils.isBlank(threadDump.getLockOwnerName())) {
            message.append(" owned by \"").append(threadDump.getLockOwnerName()).append("\" Id=").append(threadDump.getLockOwnerId());
        }

        if (threadDump.isSuspended()) {
            message.append(" (suspended)");
        }
        if (threadDump.isInNative()) {
            message.append(" (in native)");
        }
        message.append(LINE_SEPARATOR);

        // set StackTrace
        for (int i = 0; i < threadDump.getStackTraceSize(); i++) {
            String stackTrace = threadDump.getStackTrace().get(i);
            message.append(TAB_SEPARATOR + "at ").append(stackTrace);
            message.append(LINE_SEPARATOR);

            if (i == 0 && !StringUtils.isBlank(threadDump.getLockName())) {
                switch (threadState) {
                    case BLOCKED:
                        message.append(TAB_SEPARATOR + "-  blocked on ").append(threadDump.getLockName());
                        message.append(LINE_SEPARATOR);
                        break;
                    case WAITING:
                        message.append(TAB_SEPARATOR + "-  waiting on ").append(threadDump.getLockName());
                        message.append(LINE_SEPARATOR);
                        break;
                    case TIMED_WAITING:
                        message.append(TAB_SEPARATOR + "-  waiting on ").append(threadDump.getLockName());
                        message.append(LINE_SEPARATOR);
                        break;
                    default:
                }
            }

            if (threadDump.getLockedMonitors() != null) {
                for (TMonitorInfo lockedMonitor : threadDump.getLockedMonitors()) {
                    if (lockedMonitor.getStackDepth() == i) {
                        message.append(TAB_SEPARATOR + "-  locked ").append(lockedMonitor.getStackFrame());
                        message.append(LINE_SEPARATOR);
                    }
                }
            }
        }

        // set Locks
        List<String> lockedSynchronizers = threadDump.getLockedSynchronizers();
        if (!CollectionUtils.isEmpty(lockedSynchronizers)) {
            message.append(LINE_SEPARATOR + TAB_SEPARATOR + "Number of locked synchronizers = ").append(lockedSynchronizers.size());
            message.append(LINE_SEPARATOR);
            for (String lockedSynchronizer : lockedSynchronizers) {
                message.append(TAB_SEPARATOR + "- ").append(lockedSynchronizer);
                message.append(LINE_SEPARATOR);
            }
        }

        message.append(LINE_SEPARATOR);
        return message.toString();
    }

    private static TThreadState getThreadState(TThreadState threadState) {
        if (threadState == null) {
            return TThreadState.UNKNOWN;
        } else {
            return threadState;
        }
    }

}
