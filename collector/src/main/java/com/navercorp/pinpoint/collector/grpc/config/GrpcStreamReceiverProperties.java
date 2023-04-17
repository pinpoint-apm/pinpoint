/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.grpc.config;

import com.navercorp.pinpoint.collector.config.ExecutorProperties;
import com.navercorp.pinpoint.collector.receiver.BindAddress;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.server.ServerOption;

import javax.annotation.PostConstruct;

/**
 * @author Taejin Koo
 * @author emeroad
 */
public class GrpcStreamReceiverProperties extends GrpcReceiverProperties {


    private final GrpcStreamProperties streamProperties;

    public GrpcStreamReceiverProperties(boolean enable,
                                        BindAddress bindAddress,
                                        ExecutorProperties serverExecutor,
                                        ExecutorProperties serverCallExecutor,
                                        ExecutorProperties workerExecutor,
                                        ServerOption serverOption,
                                        GrpcStreamProperties streamProperties) {
        super(enable, bindAddress, serverExecutor, serverCallExecutor, workerExecutor, serverOption);
        this.streamProperties = streamProperties;
    }


    @PostConstruct
    public void log() {
        super.log();
        logger.info("streamProperties:{}", streamProperties);

        Assert.isTrue(getServerExecutor().getThreadSize() > 0, "grpcServerExecutorThreadSize must be greater than 0");
        Assert.isTrue(getServerExecutor().getQueueSize() > 0, "grpcServerExecutorQueueSize must be greater than 0");

        // Work executor
        Assert.isTrue(getWorkerExecutor().getThreadSize() > 0, "grpcWorkerExecutorThreadSize must be greater than 0");
        Assert.isTrue(getWorkerExecutor().getQueueSize() > 0, "grpcWorkerExecutorQueueSize must be greater than 0");

        Assert.isTrue(streamProperties.getSchedulerThreadSize() > 0, "grpcStreamSchedulerThreadSize must be greater than 0");

    }

    public GrpcStreamProperties getStreamProperties() {
        return streamProperties;
    }

    @Override
    public String toString() {
        return "GrpcStreamReceiverProperties{" +
                "streamConfiguration=" + streamProperties +
                "} " + super.toString();
    }
}