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

import com.navercorp.pinpoint.collector.receiver.BindAddress;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author Taejin Koo
 * @author emeroad
 */
public class GrpcReceiverProperties {

    protected final Logger logger = LogManager.getLogger(getClass());

    private final boolean enable;

    private final BindAddress bindAddress;

    private final ServerOption serverOption;

    protected GrpcReceiverProperties(boolean enable,
                                     BindAddress bindAddress,
                                     ServerOption serverOption) {
        this.enable = enable;
        this.serverOption = Objects.requireNonNull(serverOption, "serverOption");
        this.bindAddress = Objects.requireNonNull(bindAddress, "bindAddress");
    }


    @PostConstruct
    public void log() {
        this.logger.info("enable:{}", this.enable);
        this.logger.info("bindAddress:{}", bindAddress);
        this.logger.info("serverOption:{}", serverOption);
    }

    public boolean isEnable() {
        return enable;
    }

    public BindAddress getBindAddress() {
        return bindAddress;
    }


    public ServerOption getServerOption() {
        return serverOption;
    }

    @Override
    public String toString() {
        return "GrpcAgentDataReceiverProperties{" + "enable=" + enable +
                ", bindAddress='" + bindAddress + '\'' +
                ", serverOption=" + serverOption +
                '}';
    }
}