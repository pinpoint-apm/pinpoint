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

package com.navercorp.pinpoint.profiler.monitor;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.RequestUrlMappingExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.RequestUrlStatMonitor;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.monitor.storage.RequestUrlAsyncListener;
import com.navercorp.pinpoint.profiler.monitor.storage.RequestUrlStatInfo;
import com.navercorp.pinpoint.profiler.monitor.storage.TimeoutAsyncQueueingExecutor;
import com.navercorp.pinpoint.profiler.sender.DataSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class DefaultRequestUrlStatMonitor<T> implements RequestUrlStatMonitor<T> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RequestUrlMappingExtractor<T> requestUrlMappingExtractor;

    private final TimeoutAsyncQueueingExecutor timeoutAsyncQueueingExecutor;

    public DefaultRequestUrlStatMonitor(DataSender dataSender, RequestUrlMappingExtractor<T> requestUrlMappingExtractor) {
        this(dataSender, requestUrlMappingExtractor, RequestUrlAsyncListener.DEFAULT_FLUSH_INTERVAL_MILLIS);
    }

    public DefaultRequestUrlStatMonitor(DataSender dataSender, RequestUrlMappingExtractor<T> requestUrlMappingExtractor, long flushIntervalMillis) {
        Assert.requireNonNull(dataSender, "dataSender");
        // need to insert datasender  to storage

        this.requestUrlMappingExtractor = Assert.requireNonNull(requestUrlMappingExtractor, "requestUrlMappingExtractor");

        RequestUrlAsyncListener requestUrlAsyncListener = new RequestUrlAsyncListener(dataSender, flushIntervalMillis);
        this.timeoutAsyncQueueingExecutor = new TimeoutAsyncQueueingExecutor(8192, "RequestUrlStatMonitor", requestUrlAsyncListener);
    }

    @Override
    public void store(T request, String rawUrl, int status, long startTime, long endTime) {
        String url = requestUrlMappingExtractor.getUrlMapping(request, rawUrl);
        if (url == null) {
            logger.warn("can not extract url. request:{}, rawUrl:{}", request, rawUrl);
        }

        RequestUrlStatInfo requestUrlStatInfo = new RequestUrlStatInfo(url, status, startTime, endTime - startTime);
        timeoutAsyncQueueingExecutor.execute(requestUrlStatInfo);
    }

    @Override
    public void close() {
        if (timeoutAsyncQueueingExecutor != null) {
            timeoutAsyncQueueingExecutor.stop();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultRequestUrlStatMonitor{");
        sb.append("requestUrlMappingExtractor=").append(requestUrlMappingExtractor);
        sb.append(", timeoutAsyncQueueingExecutor=").append(timeoutAsyncQueueingExecutor);
        sb.append('}');
        return sb.toString();
    }

}

