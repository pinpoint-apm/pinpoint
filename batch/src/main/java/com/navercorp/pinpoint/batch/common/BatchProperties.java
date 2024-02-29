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
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author minwoo.jung<minwoo.jung@navercorp.com>
 */
@Component
public class BatchProperties {

    private final Logger logger = LogManager.getLogger(BatchProperties.class);

    @Value("${batch.server.env}")
    private String batchEnv;

    @Value("${batch.flink.server}")
    private String[] flinkServerList = new String[0];

    @Value("${batch.flink.rest.port:8081}")
    private int flinkRestPort;

    @Value("${job.alarm.enable:true}")
    private boolean alarmJobEnable;

    @Value("${job.alarm.cron}")
    private String alarmJobCron;

    @Value("${job.agent.count.enable:true}")
    private boolean agentCountJobEnable;

    @Value("${job.agent.count.cron}")
    private String agentCountJobCron;

    @Value("${job.flink.check.enable:true}")
    private boolean flinkCheckJobEnable;

    @Value("${job.flink.check.cron}")
    private String flinkCheckJobCron;

    @Value("${job.cleanup.inactive.agents.enable:true}")
    private boolean cleanupInactiveAgentsJobEnable;

    @Value("${job.cleanup.inactive.agents.cron}")
    private String cleanupInactiveAgentsJobCron;

    @Value("${job.alarm.uristat.enable:true}")
    private boolean uriStatAlarmJobEnable;

    @Value("${job.alarm.uristat.cron}")
    private String uriStatAlarmJobCron;

    @Value("${job.cleanup.inactive.agents.duration.days:30}")
    private int cleanupInactiveAgentsDurationDays;

    @Value("${job.cleanup.inactive.applications.enable:false}")
    private boolean cleanupInactiveApplicationsJobEnable;

    @Value("${job.cleanup.inactive.applications.cron}")
    private String cleanupInactiveApplicationsJobCron;

    @Value("${alarm.collector.version:1}")
    private int collectorVersion;

    private static final int MINIMUM_CLEANUP_INACTIVE_AGENTS_DURATION_DAYS = 7;

    @PostConstruct
    public void setup() {
        beforeLog();

        if (cleanupInactiveAgentsDurationDays < MINIMUM_CLEANUP_INACTIVE_AGENTS_DURATION_DAYS) {
            throw new IllegalArgumentException("'cleanupInactiveAgentsDuration' must be 'cleanupInactiveAgentsDuration >= 30'");
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

    public List<String> getFlinkServerList() {
        return Arrays.asList(flinkServerList);
    }

    public int getFlinkRestPort() {
        return flinkRestPort;
    }

    public String getBatchEnv() {
        return batchEnv;
    }

    public boolean isCleanupInactiveAgentsJobEnable() {
        return cleanupInactiveAgentsJobEnable;
    }

    public boolean isUriStatAlarmJobEnable() {
        return uriStatAlarmJobEnable;
    }

    public String getCleanupInactiveAgentsJobCron() {
        return cleanupInactiveAgentsJobCron;
    }

    public boolean isAlarmJobEnable() {
        return alarmJobEnable;
    }

    public String getAlarmJobCron() {
        return alarmJobCron;
    }

    public boolean isAgentCountJobEnable() {
        return agentCountJobEnable;
    }

    public String getAgentCountJobCron() {
        return agentCountJobCron;
    }

    public boolean isFlinkCheckJobEnable() {
        return flinkCheckJobEnable;
    }

    public String getFlinkCheckJobCron() {
        return flinkCheckJobCron;
    }

    public String getUriStatAlarmJobCron() {
        return uriStatAlarmJobCron;
    }
    public int getCleanupInactiveAgentsDurationDays() {
        return cleanupInactiveAgentsDurationDays;
    }

    public boolean isCleanupInactiveApplicationsJobEnable() {
        return cleanupInactiveApplicationsJobEnable;
    }

    public String getCleanupInactiveApplicationsJobCron() {
        return cleanupInactiveApplicationsJobCron;
    }

    public int getCollectorVersion() {
        return collectorVersion;
    }

    @Override
    public String toString() {
        return "BatchProperties{" +
                "logger=" + logger +
                ", batchEnv='" + batchEnv + '\'' +
                ", flinkServerList=" + Arrays.toString(flinkServerList) +
                ", flinkRestPort=" + flinkRestPort +
                ", alarmJobEnable=" + alarmJobEnable +
                ", alarmJobCron='" + alarmJobCron + '\'' +
                ", agentCountJobEnable=" + agentCountJobEnable +
                ", agentCountJobCron='" + agentCountJobCron + '\'' +
                ", flinkCheckJobEnable=" + flinkCheckJobEnable +
                ", flinkCheckJobCron='" + flinkCheckJobCron + '\'' +
                ", cleanupInactiveAgentsJobEnable=" + cleanupInactiveAgentsJobEnable +
                ", cleanupInactiveAgentsJobCron='" + cleanupInactiveAgentsJobCron + '\'' +
                ", uriStatAlarmJobEnable=" + uriStatAlarmJobEnable +
                ", uriStatAlarmJobCron='" + uriStatAlarmJobCron + '\'' +
                ", cleanupInactiveAgentsDurationDays=" + cleanupInactiveAgentsDurationDays +
                ", cleanupInactiveApplicationsJobEnable=" + cleanupInactiveApplicationsJobEnable +
                ", cleanupInactiveApplicationsJobCron='" + cleanupInactiveApplicationsJobCron + '\'' +
                ", collectorVersion='" + collectorVersion + '\'' +
                '}';
    }
}
