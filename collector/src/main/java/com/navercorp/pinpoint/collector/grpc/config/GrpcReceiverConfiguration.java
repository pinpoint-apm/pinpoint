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

import com.navercorp.pinpoint.collector.config.ExecutorConfiguration;
import com.navercorp.pinpoint.collector.receiver.BindAddress;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * @author Taejin Koo
 * @author emeroad
 */
public class GrpcReceiverConfiguration {

    protected final Logger logger = LogManager.getLogger(getClass());

    private final boolean enable;

    private final BindAddress bindAddress;

    private final ExecutorConfiguration serverExecutor;

    private final ExecutorConfiguration workerExecutor;

    private final ServerOption serverOption;


    GrpcReceiverConfiguration(boolean enable,
                                     BindAddress bindAddress,
                                     ExecutorConfiguration serverExecutor,
                                     ExecutorConfiguration workerExecutor,
                                     ServerOption serverOption) {
        this.enable = enable;
        this.serverOption = Objects.requireNonNull(serverOption, "serverOption");

        this.bindAddress = Objects.requireNonNull(bindAddress, "bindAddress");
        this.serverExecutor = Objects.requireNonNull(serverExecutor, "serverExecutor");
        this.workerExecutor = Objects.requireNonNull(workerExecutor, "workerExecutor");
    }


    @PostConstruct
    public void log() {
        this.logger.info("enable:{}", this.enable);
        this.logger.info("bindAddress:{}", bindAddress);
        this.logger.info("workerExecutor:{}", workerExecutor);
        this.logger.info("serverExecutor:{}", serverExecutor);
        this.logger.info("serverOption:{}", serverOption);
    }

    public boolean isEnable() {
        return enable;
    }

    public BindAddress getBindAddress() {
        return bindAddress;
    }

    public ExecutorConfiguration getServerExecutor() {
        return serverExecutor;
    }

    public ExecutorConfiguration getWorkerExecutor() {
        return workerExecutor;
    }

    public ServerOption getServerOption() {
        return serverOption;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GrpcAgentDataReceiverConfiguration{");
        sb.append("enable=").append(enable);
        sb.append(", bindAddress='").append(bindAddress).append('\'');
        sb.append(", serverExecutor=").append(serverExecutor);
        sb.append(", workerExecutor=").append(workerExecutor);
        sb.append(", serverOption=").append(serverOption);
        sb.append('}');
        return sb.toString();
    }
}