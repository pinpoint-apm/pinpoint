/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.realtime.dto;


import jakarta.annotation.Nullable;

import java.util.List;

/**
 * @author youngjin.kim2
 */
public class ThreadDump {

    private @Nullable String threadName; // required
    private long threadId; // required
    private long blockedTime; // required
    private long blockedCount; // required
    private long waitedTime; // required
    private long waitedCount; // required
    private @Nullable String lockName; // required
    private long lockOwnerId; // required
    private @Nullable String lockOwnerName; // required
    private boolean inNative; // required
    private boolean suspended; // required
    private @Nullable ThreadState threadState; // required
    private @Nullable List<String> stackTrace; // required
    private @Nullable List<MonitorInfo> lockedMonitors; // required
    private @Nullable List<String> lockedSynchronizers; // required

    @Nullable
    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(@Nullable String threadName) {
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

    @Nullable
    public String getLockName() {
        return lockName;
    }

    public void setLockName(@Nullable String lockName) {
        this.lockName = lockName;
    }

    public long getLockOwnerId() {
        return lockOwnerId;
    }

    public void setLockOwnerId(long lockOwnerId) {
        this.lockOwnerId = lockOwnerId;
    }

    @Nullable
    public String getLockOwnerName() {
        return lockOwnerName;
    }

    public void setLockOwnerName(@Nullable String lockOwnerName) {
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

    @Nullable
    public ThreadState getThreadState() {
        return threadState;
    }

    public void setThreadState(@Nullable ThreadState threadState) {
        this.threadState = threadState;
    }

    @Nullable
    public List<String> getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(@Nullable List<String> stackTrace) {
        this.stackTrace = stackTrace;
    }

    @Nullable
    public List<MonitorInfo> getLockedMonitors() {
        return lockedMonitors;
    }

    public void setLockedMonitors(@Nullable List<MonitorInfo> lockedMonitors) {
        this.lockedMonitors = lockedMonitors;
    }

    @Nullable
    public List<String> getLockedSynchronizers() {
        return lockedSynchronizers;
    }

    public void setLockedSynchronizers(@Nullable List<String> lockedSynchronizers) {
        this.lockedSynchronizers = lockedSynchronizers;
    }
}
