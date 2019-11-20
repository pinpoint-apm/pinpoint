/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.web.batch;

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

/**
 * @author minwoo.jung<minwoo.jung@navercorp.com>
 *
 */
@Configuration
public class BatchConfiguration {

    private final Logger logger = LoggerFactory.getLogger(BatchConfiguration.class);

    @Value("${batch.enable:false}")
    private boolean enableBatch;

    @Value("${batch.flink.server}")
    private String[] flinkServerList = new String[0];

    @Value("${batch.server.ip:#{null}}")
    private String batchServerIp;

    @Value("${alarm.mail.server.url}")
    private String emailServerUrl;

    @Value("${alarm.mail.sender.address}")
    private String senderEmailAddress;

    @Value("${pinpoint.url}")
    private String pinpointUrl;

    @Value("${batch.server.env}")
    private String batchEnv;


    @PostConstruct
    public void log() {
        logger.info("{}", this);
        AnnotationVisitor annotationVisitor = new AnnotationVisitor(Value.class);
        annotationVisitor.visit(this, new LoggingEvent(this.logger));
    }

    public String getPinpointUrl() {
        return pinpointUrl;
    }

    public String getBatchServerIp() {
        return batchServerIp;
    }

    public List<String> getFlinkServerList() {
        return Arrays.asList(flinkServerList);
    }

    public String getEmailServerUrl() {
        return emailServerUrl;
    }

    public String getBatchEnv() {
        return batchEnv;
    }

    public String getSenderEmailAddress() {
        return senderEmailAddress;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BatchConfiguration{");
        sb.append("enableBatch=").append(enableBatch);
        sb.append(", flinkServerList=").append(Arrays.toString(flinkServerList));
        sb.append(", batchServerIp='").append(batchServerIp).append('\'');
        sb.append(", emailServerUrl='").append(emailServerUrl).append('\'');
        sb.append(", senderEmailAddress='").append(senderEmailAddress).append('\'');
        sb.append(", pinpointUrl='").append(pinpointUrl).append('\'');
        sb.append(", batchEnv='").append(batchEnv).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
