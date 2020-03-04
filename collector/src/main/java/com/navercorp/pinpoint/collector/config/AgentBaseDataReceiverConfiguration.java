/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import com.navercorp.pinpoint.common.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author Taejin Koo
 */
@Configuration
public class AgentBaseDataReceiverConfiguration {
    private final Logger logger = LoggerFactory.getLogger(AgentBaseDataReceiverConfiguration.class);

    @Value("${collector.receiver.base.ip:0.0.0.0}")
    private String bindIp;

    @Value("${collector.receiver.base.port:9994}")
    private int bindPort;

    @Value("${collector.receiver.base.worker.threadSize:128}")
    private int workerThreadSize;

    @Value("${collector.receiver.base.worker.queueSize:5120}")
    private int workerQueueSize;

    @Value("${collector.receiver.base.worker.monitor:false}")
    private boolean workerMonitorEnable;


    public AgentBaseDataReceiverConfiguration() {
    }



    @PostConstruct
    public void log() {
        logger.info("{}", this);
        AnnotationVisitor visitor = new AnnotationVisitor(Value.class);
        visitor.visit(this, new LoggingEvent(logger));

        validate();
    }

    private void validate() {
        Assert.isTrue(bindPort > 0, "bindPort must be greater than 0");
        Assert.isTrue(workerThreadSize > 0, "workerThreadSize must be greater than 0");
        Assert.isTrue(workerQueueSize > 0, "workerQueueSize must be greater than 0");
    }

    public String getBindIp() {
        return bindIp;
    }


    public int getBindPort() {
        return bindPort;
    }

    public int getWorkerThreadSize() {
        return workerThreadSize;
    }

    public int getWorkerQueueSize() {
        return workerQueueSize;
    }

    public boolean isWorkerMonitorEnable() {
        return workerMonitorEnable;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentBaseDataReceiverConfiguration{");
        sb.append("bindIp='").append(bindIp).append('\'');
        sb.append(", bindPort=").append(bindPort);
        sb.append(", workerThreadSize=").append(workerThreadSize);
        sb.append(", workerQueueSize=").append(workerQueueSize);
        sb.append(", workerMonitorEnable=").append(workerMonitorEnable);
        sb.append('}');
        return sb.toString();
    }
}