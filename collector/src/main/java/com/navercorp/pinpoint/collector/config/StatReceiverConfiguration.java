/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import com.navercorp.pinpoint.common.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Configuration
public class StatReceiverConfiguration implements DataReceiverGroupConfiguration {

    private final Logger logger = LoggerFactory.getLogger(StatReceiverConfiguration.class);

    private static final String PREFIX = "collector.receiver.stat";

    @Value("${collector.receiver.stat.tcp:false}")
    private boolean isTcpEnable;

    @Value("${collector.receiver.stat.tcp.ip:0.0.0.0}")
    private String tcpBindIp;

    @Value("${collector.receiver.stat.tcp.port:-1}")
    private int tcpBindPort;

    @Value("${collector.receiver.stat.udp:true}")
    private boolean isUdpEnable;

    @Value("${collector.receiver.stat.udp.ip:0.0.0.0}")
    private String udpBindIp;

    @Value("${collector.receiver.stat.udp.port:9995}")
    private int udpBindPort;

    @Value("${collector.receiver.stat.udp.receiveBufferSize:" + 1024 * 4096 + "}")
    private int udpReceiveBufferSize;

    @Value("${collector.receiver.stat.udp.socket.count:-1}")
    private int socketCount;

    @Value("${collector.receiver.stat.udp.reuseport:false}")
    private boolean reusePort;

    @Value("${collector.receiver.stat.worker.threadSize:128}")
    private int workerThreadSize;

    @Value("${collector.receiver.stat.worker.queueSize:5120}")
    private int workerQueueSize;

    @Value("${collector.receiver.stat.worker.monitor:false}")
    private boolean workerMonitorEnable;

    @PostConstruct
    public void log() {
        logger.info("{}", this);
        AnnotationVisitor visitor = new AnnotationVisitor(Value.class);
        visitor.visit(this, new LoggingEvent(logger));
        
        validate();
    }

    private void validate() {
        Assert.isTrue(workerThreadSize > 0, "workerThreadSize must be greater than 0");
        Assert.isTrue(workerQueueSize > 0, "workerQueueSize must be greater than 0");
        Assert.isTrue(isTcpEnable || isUdpEnable, "statReceiver does not allow tcp and udp disable");

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StatReceiverConfiguration{");
        sb.append("isTcpEnable=").append(isTcpEnable);
        sb.append(", tcpBindIp='").append(tcpBindIp).append('\'');
        sb.append(", tcpBindPort=").append(tcpBindPort);
        sb.append(", isUdpEnable=").append(isUdpEnable);
        sb.append(", udpBindIp='").append(udpBindIp).append('\'');
        sb.append(", udpBindPort=").append(udpBindPort);
        sb.append(", udpReceiveBufferSize=").append(udpReceiveBufferSize);
        sb.append(", socketCount=").append(socketCount);
        sb.append(", reusePort=").append(reusePort);
        sb.append(", workerThreadSize=").append(workerThreadSize);
        sb.append(", workerQueueSize=").append(workerQueueSize);
        sb.append(", workerMonitorEnable=").append(workerMonitorEnable);
        sb.append('}');
        return sb.toString();
    }

}