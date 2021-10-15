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

package com.navercorp.pinpoint.collector.thrift.config;

import com.navercorp.pinpoint.collector.config.ExecutorConfiguration;
import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import com.navercorp.pinpoint.common.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Component("spanReceiverConfig")
public class SpanReceiverConfiguration implements DataReceiverGroupConfiguration {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${collector.receiver.span.tcp:false}")
    private boolean isTcpEnable;

    @Value("${collector.receiver.span.tcp.ip:0.0.0.0}")
    private String tcpBindIp;

    @Value("${collector.receiver.span.tcp.port:-1}")
    private int tcpBindPort;

    @Value("${collector.receiver.span.udp:true}")
    private boolean isUdpEnable;

    @Value("${collector.receiver.span.udp.ip:0.0.0.0}")
    private String udpBindIp;

    @Value("${collector.receiver.span.udp.port:9996}")
    private int udpBindPort;

    @Value("${collector.receiver.span.udp.receiveBufferSize:" + (1024 * 4096) + "}")
    private int udpReceiveBufferSize;

    @Value("${collector.receiver.span.udp.reuseport:false}")
    private boolean reusePort;

    @Value("${collector.receiver.span.udp.socket.count:-1}")
    private int socketCount;

    @Value("${collector.receiver.span.worker.threadSize:256}")
    private int workerThreadSize;

    @Value("${collector.receiver.span.worker.queueSize:5120}")
    private int workerQueueSize;

    @Value("${collector.receiver.span.worker.monitor:true}")
    private boolean workerMonitorEnable;


    public SpanReceiverConfiguration() {
    }

    @PostConstruct
    public  void validate() {
        Assert.isTrue(workerThreadSize > 0, "workerThreadSize must be greater than 0");
        Assert.isTrue(workerQueueSize > 0, "workerQueueSize must be greater than 0");

        Assert.isTrue(isTcpEnable || isUdpEnable, "spanReceiver does not allow tcp and udp disable");

        if (isTcpEnable) {
            Objects.requireNonNull(tcpBindIp, "tcpBindIp");
            Assert.isTrue(tcpBindPort > 0, "tcpBindPort must be greater than 0");
        }

        if (isUdpEnable) {
            Objects.requireNonNull(udpBindIp, "udpBindIp");
            Assert.isTrue(udpBindPort > 0, "udpBindPort must be greater than 0");
            Assert.isTrue(udpReceiveBufferSize > 0, "udpReceiveBufferSize must be greater than 0");
        }
    }

    @PostConstruct
    public void log() {
        logger.info("{}", this);

        AnnotationVisitor<Value> visitor = new AnnotationVisitor<>(Value.class);
        visitor.visit(this, new LoggingEvent(logger));
    }

    @Override
    public boolean isTcpEnable() {
        return isTcpEnable;
    }

    @Override
    public String getTcpBindIp() {
        return tcpBindIp;
    }

    @Override
    public int getTcpBindPort() {
        return tcpBindPort;
    }

    @Override
    public boolean isUdpEnable() {
        return isUdpEnable;
    }

    @Override
    public String getUdpBindIp() {
        return udpBindIp;
    }

    @Override
    public int getUdpBindPort() {
        return udpBindPort;
    }

    @Override
    public int getUdpReceiveBufferSize() {
        return udpReceiveBufferSize;
    }

    @Override
    public boolean isReusePort() {
        return reusePort;
    }

    @Override
    public int getSocketCount() {
        return socketCount;
    }

    @Override
    public int getWorkerThreadSize() {
        return workerThreadSize;
    }

    @Override
    public int getWorkerQueueSize() {
        return workerQueueSize;
    }

    @Override
    public boolean isWorkerMonitorEnable() {
        return workerMonitorEnable;
    }

    @Bean("spanExecutorConfiguration")
    public ExecutorConfiguration newExecutorConfiguration() {
        return new ExecutorConfiguration(workerThreadSize, workerQueueSize, workerMonitorEnable);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpanReceiverConfiguration{");
        sb.append("isTcpEnable=").append(isTcpEnable);
        sb.append(", tcpBindIp='").append(tcpBindIp).append('\'');
        sb.append(", tcpBindPort=").append(tcpBindPort);
        sb.append(", isUdpEnable=").append(isUdpEnable);
        sb.append(", udpBindIp='").append(udpBindIp).append('\'');
        sb.append(", udpBindPort=").append(udpBindPort);
        sb.append(", udpReceiveBufferSize=").append(udpReceiveBufferSize);
        sb.append(", reusePort=").append(reusePort);
        sb.append(", socketCount=").append(socketCount);
        sb.append(", workerThreadSize=").append(workerThreadSize);
        sb.append(", workerQueueSize=").append(workerQueueSize);
        sb.append(", workerMonitorEnable=").append(workerMonitorEnable);
        sb.append('}');
        return sb.toString();
    }

}