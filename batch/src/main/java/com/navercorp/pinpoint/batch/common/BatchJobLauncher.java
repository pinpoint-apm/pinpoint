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

import com.navercorp.pinpoint.batch.config.CleanupAgentAndApplicationJobConfig;
import com.navercorp.pinpoint.batch.util.JobParametersUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;

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
        return JobParametersUtils.newJobParametersBuilder().toJobParameters();
    }

    public void agentCountJob() {
        if (batchProperties.isAgentCountJobEnable()) {
            run("agentCountJob", createTimeParameter());
        } else {
            logger.debug("Skip agentCountJob, because 'enableAgentCountJob' is disabled.");
        }
    }

    public void cleanupInactiveApplicationsJob() {
        if (batchProperties.isCleanupInactiveApplicationsJobEnable()) {
            run("cleanupInactiveApplicationsJob", createTimeParameter());
        } else {
            logger.debug("Skip applicationCleanJob, because 'enableCleanupInactiveApplicationsJob' is disabled.");
        }
    }

    public void cleanAgentAndApplicationJob() {
        if (batchProperties.isCleanupAgentAndApplicationJobEnable()) {
            run(CleanupAgentAndApplicationJobConfig.JOB_NAME, createTimeParameter(batchProperties.isCleanupAgentAndApplicationJobDryRun()));
        } else {
            logger.debug("Skip {}, because 'agentIdCleanJobEnable' is disabled.", CleanupAgentAndApplicationJobConfig.JOB_NAME);
        }
    }

    public static JobParameters createTimeParameter(boolean dryRun) {
        JobParametersBuilder builder = JobParametersUtils.newJobParametersBuilder();
        builder.addString("dryRun", String.valueOf(dryRun));
        return builder.toJobParameters();
    }

}
