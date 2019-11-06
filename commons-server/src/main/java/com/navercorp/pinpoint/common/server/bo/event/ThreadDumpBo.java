/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.event;

import java.util.List;

/**
 * @author jaehong.kim
 */
public class ThreadDumpBo {
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
    private ThreadState threadState;
    private List<String> stackTraceList;
    private List<MonitorInfoBo> lockedMonitorInfoList;
    private List<String> lockedSynchronizerList;

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

    public ThreadState getThreadState() {
        return threadState;
    }

    public void setThreadState(ThreadState threadState) {
        this.threadState = threadState;
    }

    public List<String> getStackTraceList() {
        return stackTraceList;
    }

    public void setStackTraceList(List<String> stackTraceList) {
        this.stackTraceList = stackTraceList;
    }

    public List<MonitorInfoBo> getLockedMonitorInfoList() {
        return lockedMonitorInfoList;
    }

    public void setLockedMonitorInfoList(List<MonitorInfoBo> lockedMonitorInfoList) {
        this.lockedMonitorInfoList = lockedMonitorInfoList;
    }

    public List<String> getLockedSynchronizerList() {
        return lockedSynchronizerList;
    }

    public void setLockedSynchronizerList(List<String> lockedSynchronizerList) {
        this.lockedSynchronizerList = lockedSynchronizerList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThreadDumpBo{");
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
        sb.append(", stackTraceList=").append(stackTraceList);
        sb.append(", lockedMonitorInfoList=").append(lockedMonitorInfoList);
        sb.append(", lockedSynchronizerList=").append(lockedSynchronizerList);
        sb.append('}');
        return sb.toString();
    }
}
