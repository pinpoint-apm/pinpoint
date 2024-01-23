/*
 * Copyright 2014 NAVER Corp.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Date;
import java.util.Objects;

/**
 * @author minwoo.jung<minwoo.jung@navercorp.com>
 */
public class BatchJobLauncher extends JobLaunchSupport {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final BatchProperties batchProperties;

    public BatchJobLauncher(
            @Qualifier("jobRegistry") JobLocator locator,
            @Qualifier("jobLauncher") JobLauncher launcher,
            BatchProperties batchProperties
    ) {
        super(locator, launcher);
        this.batchProperties = Objects.requireNonNull(batchProperties, "batchProperties");
    }

    public void alarmJob() {
        if (batchProperties.isAlarmJobEnable()) {
            run("alarmJob", createTimeParameter());
        } else {
            logger.debug("Skip alarmJob, because 'enableUriStatAlarmJob' is disabled.");
        }

    }

    public void uriStatAlarmJob() {
        if (batchProperties.isUriStatAlarmJobEnable()) {
            run("uriAlarmJob", createTimeParameter());
        } else {
            logger.debug("Skip uriAlarmJob, because 'enableUriStatAlarmJob' is disabled.");
        }
    }

    public static JobParameters createTimeParameter() {
        JobParametersBuilder builder = new JobParametersBuilder();
        Date now = new Date();
        builder.addDate("schedule.date", now);
        return builder.toJobParameters();
    }

    public void agentCountJob() {
        if (batchProperties.isAgentCountJobEnable()) {
            run("agentCountJob", createTimeParameter());
        } else {
            logger.debug("Skip agentCountJob, because 'enableAgentCountJob' is disabled.");
        }
    }

    public void flinkCheckJob() {
        if (batchProperties.isFlinkCheckJobEnable()) {
            run("flinkCheckJob", createTimeParameter());
        } else {
            logger.debug("Skip flinkCheckJob, because 'enableFlinkCheckJob' is disabled.");
        }
    }

    public void cleanupInactiveAgentsJob() {
        if (batchProperties.isCleanupInactiveAgentsJobEnable()) {
            run("cleanupInactiveAgentsJob", createTimeParameter());
        } else {
            logger.debug("Skip cleanupInactiveAgentsJob, because 'enableCleanupInactiveAgentsJob' is disabled.");
        }
    }

    public void cleanupInactiveApplicationsJob() {
        if (batchProperties.isCleanupInactiveApplicationsJobEnable()) {
            run("cleanupInactiveApplicationsJob", createTimeParameter());
        } else {
            logger.debug("Skip applicationCleanJob, because 'enableCleanupInactiveApplicationsJob' is disabled.");
        }
    }

}
