/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcUriStatMessageConverter;
import com.navercorp.pinpoint.profiler.context.storage.UriStatStorage;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.AgentUriStatData;
import com.navercorp.pinpoint.profiler.sender.DataSender;

import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

/**
 * @author Taejin Koo
 */
public class UriStatCollectingJob implements Runnable, Closeable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isTraceable = logger.isTraceEnabled();

    private final DataSender dataSender;
    private final UriStatStorage uriStatStorage;

    public UriStatCollectingJob(DataSender dataSender, UriStatStorage uriStatStorage) {
        this.dataSender = Assert.requireNonNull(dataSender, "dataSender");
        this.uriStatStorage = Assert.requireNonNull(uriStatStorage, "uriStatStorage");
    }

    @Override
    public void run() {

        while (true) {
            AgentUriStatData agentUriStatData = uriStatStorage.poll();
            if (agentUriStatData == null) {
                break;
            }

            dataSender.send(agentUriStatData);
        }
    }

    @Override
    public void close() {
        try {
            uriStatStorage.close();
        } catch (Exception e) {
            // do nothing
        }
    }

}
