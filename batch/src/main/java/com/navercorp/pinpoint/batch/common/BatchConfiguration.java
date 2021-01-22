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
package com.navercorp.pinpoint.batch.common;

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
 */
@Configuration
public class BatchConfiguration {

    private final Logger logger = LoggerFactory.getLogger(BatchConfiguration.class);

    @Value("${alarm.mail.server.url}")
    private String emailServerUrl;

    @Value("${alarm.mail.sender.address}")
    private String senderEmailAddress;

    @Value("${pinpoint.url}")
    private String pinpointUrl;

    @Value("${batch.server.env}")
    private String batchEnv;

    @Value("${batch.flink.server}")
    private String[] flinkServerList = new String[0];


    @Value("${job.cleanup.inactive.agents:false}")
    private boolean enableCleanupInactiveAgents;

    private static final int DEFAULT_CLEANUP_INACTIVE_AGENTS_DURATION_DAYS = 30;
    private static final int MINIMUM_CLEANUP_INACTIVE_AGENTS_DURATION_DAYS = 7;

    @Value("${job.cleanup.inactive.agents.duration.days:30}")
    private int cleanupInactiveAgentsDurationDays;

    // Spring supports `org.springframework.scheduling.annotation.Scheduled#CRON_DISABLED` since Spring 5.x
    // https://github.com/spring-projects/spring-framework/issues/21397
    // BatchLauner does not run a job even if cron is executed. Nevertheless, Cron should be disabled if possible.
    private static final String DISABLED_CLEANUP_INACTIVE_AGENTS_CRON = "0 0 0 29 2 ?";

    @Value("${job.cleanup.inactive.agents.cron:}")
    private String cleanupInactiveAgentsCron;


    @PostConstruct
    public void setup() {
        beforeLog();

        if (enableCleanupInactiveAgents == false) {
            cleanupInactiveAgentsDurationDays = DEFAULT_CLEANUP_INACTIVE_AGENTS_DURATION_DAYS;
            cleanupInactiveAgentsCron = DISABLED_CLEANUP_INACTIVE_AGENTS_CRON;
        } else {
            if (cleanupInactiveAgentsDurationDays < MINIMUM_CLEANUP_INACTIVE_AGENTS_DURATION_DAYS) {
                throw new IllegalArgumentException("'cleanupInactiveAgentsDuration' must be 'cleanupInactiveAgentsDuration >= 30'");
            }
        }

        afterLog();
    }

    private void beforeLog() {
        logger.info("before setup field: {}", this);
        AnnotationVisitor<Value> annotationVisitor = new AnnotationVisitor<>(Value.class);
        annotationVisitor.visit(this, new LoggingEvent(this.logger));
    }

    private void afterLog() {
        logger.info("after setup field : {}", this);
        AnnotationVisitor<Value> annotationVisitor = new AnnotationVisitor<>(Value.class);
        annotationVisitor.visit(this, new LoggingEvent(this.logger));
    }


    public String getPinpointUrl() {
        return pinpointUrl;
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

    public boolean isEnableCleanupInactiveAgents() {
        return enableCleanupInactiveAgents;
    }

    public int getCleanupInactiveAgentsDurationDays() {
        return cleanupInactiveAgentsDurationDays;
    }

    public String getCleanupInactiveAgentsCron() {
        return cleanupInactiveAgentsCron;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BatchConfiguration{");
        sb.append("emailServerUrl='").append(emailServerUrl).append('\'');
        sb.append(", senderEmailAddress='").append(senderEmailAddress).append('\'');
        sb.append(", pinpointUrl='").append(pinpointUrl).append('\'');
        sb.append(", batchEnv='").append(batchEnv).append('\'');
        sb.append(", flinkServerList=").append(Arrays.toString(flinkServerList));
        sb.append(", enableCleanupInactiveAgents=").append(enableCleanupInactiveAgents);
        sb.append(", cleanupInactiveAgentsDurationDays=").append(cleanupInactiveAgentsDurationDays);
        sb.append(", cleanupInactiveAgentsCron='").append(cleanupInactiveAgentsCron).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
