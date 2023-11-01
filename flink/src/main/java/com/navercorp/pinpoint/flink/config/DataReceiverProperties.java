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

package com.navercorp.pinpoint.flink.config;

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import com.navercorp.pinpoint.common.util.Assert;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Taejin Koo
 */
public class DataReceiverProperties {
    private final Logger logger = LogManager.getLogger(getClass());

    @Value("${flink.receiver.base.ip:0.0.0.0}")
    private String bindIp;

    @Value("${flink.receiver.base.port:9994}")
    private int bindPort;


    public DataReceiverProperties() {
    }



    @PostConstruct
    public void log() {
        logger.info("{}", this);
        AnnotationVisitor<Value> visitor = new AnnotationVisitor<>(Value.class);
        visitor.visit(this, new LoggingEvent(logger));

        validate();
    }

    private void validate() {
        Assert.isTrue(bindPort > 0, "bindPort must be greater than 0");
    }

    public String getBindIp() {
        return bindIp;
    }


    public int getBindPort() {
        return bindPort;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentBaseDataReceiverProperties{");
        sb.append("bindIp='").append(bindIp).append('\'');
        sb.append(", bindPort=").append(bindPort);
        sb.append('}');
        return sb.toString();
    }
}