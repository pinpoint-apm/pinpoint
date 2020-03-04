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

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDump;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDump;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ThreadDumpRequest {

    // TODO extract config
    // See DefaultActiveTraceRepository.DEFAULT_MAX_ACTIVE_TRACE_SIZE = 1024 * 10
    static final int MAX_THREAD_DUMP_LIMIT = 1024 * 2;

    private final Set<Long> localTransactionIdSet;
    private final Set<String> threadNameSet;
    private final int limit;
    private final boolean enableFilter;
    private final boolean localTransactionIdFilter;
    private final boolean threadNameFilter;
    private final StackTrace stackTrace;


    public static ThreadDumpRequest create(TCmdActiveThreadDump request) {
        Assert.requireNonNull(request, "request");

        final int limit = getLimit(request.getLimit());

        final List<Long> localTransactionIdList = request.getLocalTraceIdList();
        final List<String> threadNameList = request.getThreadNameList();

        return new ThreadDumpRequest(StackTrace.DUMP, limit, localTransactionIdList, threadNameList);
    }

    public static ThreadDumpRequest create(TCmdActiveThreadLightDump request) {
        Assert.requireNonNull(request, "request");

        int limit = getLimit(request.getLimit());

        final List<Long> localTransactionIdList = request.getLocalTraceIdList();
        final List<String> threadNameList = request.getThreadNameList();

        return new ThreadDumpRequest(StackTrace.SKIP, limit, localTransactionIdList, threadNameList);
    }

    public static ThreadDumpRequest create(PCmdActiveThreadDump request) {
        Assert.requireNonNull(request, "request");

        int limit = getLimit(request.getLimit());

        final List<Long> localTransactionIdList = request.getLocalTraceIdList();
        final List<String> threadNameList = request.getThreadNameList();

        return new ThreadDumpRequest(StackTrace.DUMP, limit, localTransactionIdList, threadNameList);
    }

    public static ThreadDumpRequest create(PCmdActiveThreadLightDump request) {
        Assert.requireNonNull(request, "request");

        int limit = getLimit(request.getLimit());

        final List<Long> localTransactionIdList = request.getLocalTraceIdList();
        final List<String> threadNameList = request.getThreadNameList();

        return new ThreadDumpRequest(StackTrace.SKIP, limit, localTransactionIdList, threadNameList);
    }

    ThreadDumpRequest(StackTrace stackTrace, int limit, List<Long> localTransactionIdList, List<String> threadNameList) {
        this.stackTrace = Assert.requireNonNull(stackTrace, "stackTrace");
        this.limit = limit;

        this.localTransactionIdSet = newHashSet(localTransactionIdList);
        this.localTransactionIdFilter = CollectionUtils.hasLength(localTransactionIdSet);

        this.threadNameSet = newHashSet(threadNameList);
        this.threadNameFilter = CollectionUtils.hasLength(threadNameSet);

        this.enableFilter = isEnableFilter(localTransactionIdFilter, threadNameFilter);
    }


    private boolean isEnableFilter(boolean localTransactionIdFilter, boolean threadNameFilter) {
        if (localTransactionIdFilter) {
            return true;
        }
        if (threadNameFilter) {
            return true;
        }

        return false;
    }

    @VisibleForTesting
    static int getLimit(int limit) {
        if (0 >= limit) {
            return MAX_THREAD_DUMP_LIMIT;
        }
        return Math.min(limit, MAX_THREAD_DUMP_LIMIT);
    }

    private <T> Set<T> newHashSet(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptySet();
        }
        return new HashSet<T>(list);
    }

    public int getLimit() {
        return limit;
    }

    public boolean isEnableFilter() {
        return enableFilter;
    }

    public boolean isEnableLocalTransactionIdFilter() {
        return localTransactionIdFilter;
    }

    public boolean findLocalTransactionId(long localTransactionId) {
        return this.localTransactionIdSet.contains(localTransactionId);
    }

    public boolean isEnableThreadNameFilter() {
        return threadNameFilter;
    }

    public boolean findThreadName(String threadName) {
        return this.threadNameSet.contains(threadName);
    }

    public StackTrace getStackTrace() {
        return this.stackTrace;
    }
}
