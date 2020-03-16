/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.storage;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.trace.PRequestUrlStatBatch;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcRequestUrlStatBatchConverter;
import com.navercorp.pinpoint.profiler.sender.DataSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class RequestUrlAsyncListener implements TimeoutAsyncQueueingExecutorListener<RequestUrlStatInfo> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    static final RequestUrlStatInfo TIMEOUT_REQUESTS_URL_STAT_INFO = new RequestUrlStatInfo("", -1, -1, -1);

    public static final long DEFAULT_FLUSH_INTERVAL_MILLIS = 1000 * 60;
    private static final int DEFAULT_MAX_WATER_MARK_SIZE = 65535;

    private final GrpcRequestUrlStatBatchConverter converter = new GrpcRequestUrlStatBatchConverter();
    private DataSizeCheckStorage dataSizeCheckStorage = new DataSizeCheckStorage(DEFAULT_MAX_WATER_MARK_SIZE);

    private final long flushIntervalMs;
    private long lastFlushedTime = System.currentTimeMillis();

    private final DataSender dataSender;

    public RequestUrlAsyncListener(DataSender dataSender) {
        this(dataSender, DEFAULT_FLUSH_INTERVAL_MILLIS);
    }

    public RequestUrlAsyncListener(DataSender dataSender, long flushIntervalMillis) {
        this.dataSender = Assert.requireNonNull(dataSender, "dataSender");

        Assert.isTrue(flushIntervalMillis > 0, "'flushIntervalMillis' must be > 0" );
        this.flushIntervalMs = flushIntervalMillis;
    }

    @Override
    public void execute(Collection<RequestUrlStatInfo> messageList) {
        Object[] messages = messageList.toArray();

        int dataSize = messageList.size();
        for (int i = 0; i < dataSize; i++) {
            dataSizeCheckStorage.store((RequestUrlStatInfo) messages[i]);
        }

        if (dataSizeCheckStorage.isOverDataSize()) {
            List<RequestUrlStatInfo> requestUrlStatInfoList = dataSizeCheckStorage.getAndClear();
            PRequestUrlStatBatch requestUrlStatBatch = converter.toMessage(requestUrlStatInfoList);
            send(requestUrlStatBatch);
        }
    }

    @Override
    public void execute(RequestUrlStatInfo requestUrlStatInfo) {
        if (isTimeoutMessage(requestUrlStatInfo)) {
            timeout();
            return;
        }

        dataSizeCheckStorage.store(requestUrlStatInfo);
        if (dataSizeCheckStorage.isOverDataSize()) {
            List<RequestUrlStatInfo> requestUrlStatInfoList = dataSizeCheckStorage.getAndClear();
            PRequestUrlStatBatch requestUrlStatBatch = converter.toMessage(requestUrlStatInfoList);
            send(requestUrlStatBatch);
        }
    }

    private boolean isTimeoutMessage(RequestUrlStatInfo message) {
        return message == TIMEOUT_REQUESTS_URL_STAT_INFO;
    }

    private void timeout() {
        boolean hasData = dataSizeCheckStorage.hasData();
        if (hasData) {
            List<RequestUrlStatInfo> requestUrlStatInfoList = dataSizeCheckStorage.getAndClear();
            PRequestUrlStatBatch requestUrlStatBatch = converter.toMessage(requestUrlStatInfoList);
            send(requestUrlStatBatch);
        }
        this.lastFlushedTime = System.currentTimeMillis();
    }

    @Override
    public long getRemainingTime() {
        long waitTime = lastFlushedTime + flushIntervalMs - System.currentTimeMillis();
        if (waitTime > 0) {
            return waitTime;
        } else {
            return 0;
        }
    }

    private void send(PRequestUrlStatBatch requestUrlStatBatch) {
        logger.debug("send. data:{}", requestUrlStatBatch);
        dataSender.send(requestUrlStatBatch);
        this.lastFlushedTime = System.currentTimeMillis();
    }

}
