/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.monitor.metric.deadlock;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class ThreadDumpMetricSnapshot {
    private String threadName;
    private long threadId;
    private long blockedTime;
    private long blockedCount;
    private long waitedTime;
    private long waitedCount;
    private String lockName;
    private long lockOwnerId;
    private String lockOwnerName;
    private boolean inNative;
    private boolean suspended;
    private Thread.State threadState;
    private List<String> stackTrace = new ArrayList<String>();
    private List<MonitorInfoMetricSnapshot> lockedMonitors = new ArrayList<MonitorInfoMetricSnapshot>();
    private List<String> lockedSynchronizers = new ArrayList<String>();

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public long getBlockedTime() {
        return blockedTime;
    }

    public void setBlockedTime(long blockedTime) {
        this.blockedTime = blockedTime;
    }

    public long getBlockedCount() {
        return blockedCount;
    }

    public void setBlockedCount(long blockedCount) {
        this.blockedCount = blockedCount;
    }

    public long getWaitedTime() {
        return waitedTime;
    }

    public void setWaitedTime(long waitedTime) {
        this.waitedTime = waitedTime;
    }

    public long getWaitedCount() {
        return waitedCount;
    }

    public void setWaitedCount(long waitedCount) {
        this.waitedCount = waitedCount;
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public long getLockOwnerId() {
        return lockOwnerId;
    }

    public void setLockOwnerId(long lockOwnerId) {
        this.lockOwnerId = lockOwnerId;
    }

    public String getLockOwnerName() {
        return lockOwnerName;
    }

    public void setLockOwnerName(String lockOwnerName) {
        this.lockOwnerName = lockOwnerName;
    }

    public boolean isInNative() {
        return inNative;
    }

    public void setInNative(boolean inNative) {
        this.inNative = inNative;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public Thread.State getThreadState() {
        return threadState;
    }

    public void setThreadState(Thread.State threadState) {
        this.threadState = threadState;
    }

    public List<String> getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(List<String> stackTrace) {
        this.stackTrace = stackTrace;
    }

    public void addStackTrace(String stackTrace) {
        this.stackTrace.add(stackTrace);
    }

    public List<MonitorInfoMetricSnapshot> getLockedMonitors() {
        return lockedMonitors;
    }

    public void addLockedMonitor(MonitorInfoMetricSnapshot monitorInfoMetricSnapshot) {
        this.lockedMonitors.add(monitorInfoMetricSnapshot);
    }

    public void setLockedMonitors(List<MonitorInfoMetricSnapshot> monitorInfoMetricSnapshotList) {
        this.lockedMonitors = monitorInfoMetricSnapshotList;
    }

    public List<String> getLockedSynchronizers() {
        return lockedSynchronizers;
    }

    public void addLockedSynchronizer(String lockedSynchronizer) {
        this.lockedSynchronizers.add(lockedSynchronizer);
    }

    public void setLockedSynchronizers(List<String> lockedSynchronizers) {
        this.lockedSynchronizers = lockedSynchronizers;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThreadDumpMetricSnapshot{");
        sb.append("threadName='").append(threadName).append('\'');
        sb.append(", threadId=").append(threadId);
        sb.append(", blockedTime=").append(blockedTime);
        sb.append(", blockedCount=").append(blockedCount);
        sb.append(", waitedTime=").append(waitedTime);
        sb.append(", waitedCount=").append(waitedCount);
        sb.append(", lockName='").append(lockName).append('\'');
        sb.append(", lockOwnerId=").append(lockOwnerId);
        sb.append(", lockOwnerName='").append(lockOwnerName).append('\'');
        sb.append(", inNative=").append(inNative);
        sb.append(", suspended=").append(suspended);
        sb.append(", threadState=").append(threadState);
        sb.append(", stackTrace=").append(stackTrace);
        sb.append(", lockedMonitors=").append(lockedMonitors);
        sb.append(", lockedSynchronizers=").append(lockedSynchronizers);
        sb.append('}');
        return sb.toString();
    }
}