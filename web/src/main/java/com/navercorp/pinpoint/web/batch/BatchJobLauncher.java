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

package com.navercorp.pinpoint.web.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * @author minwoo.jung<minwoo.jung@navercorp.com>
 */
@Deprecated
public class BatchJobLauncher extends JobLaunchSupport {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String CLEANUP_INACTIVE_AGENTS_JOB_NAME = "cleanupInactiveAgentsJob";

    @Autowired
    private BatchConfiguration batchConfiguration;

    private boolean enableCleanupInactiveAgentsJob;

    @PostConstruct
    private void setup() {
        boolean enableCleanupInactiveAgents = batchConfiguration.isEnableCleanupInactiveAgents();
        this.enableCleanupInactiveAgentsJob = enableCleanupInactiveAgents;
    }

    public void alarmJob() {
        JobParameters params = createTimeParameter();
        run("alarmJob", params);
    }

    private JobParameters createTimeParameter() {
        JobParametersBuilder builder = new JobParametersBuilder();
        Date now = new Date();
        builder.addDate("schedule.date", now);
        return builder.toJobParameters();
    }

    public void agentCountJob() {
        run("agentCountJob", createTimeParameter());
    }

    public void flinkCheckJob() {
        run("flinkCheckJob", createTimeParameter());
    }

    public void cleanupInactiveAgentsJob() {
        if (enableCleanupInactiveAgentsJob) {
            run(CLEANUP_INACTIVE_AGENTS_JOB_NAME, createTimeParameter());
        } else {
            logger.debug("Skip " + CLEANUP_INACTIVE_AGENTS_JOB_NAME + ", because 'enableCleanupInactiveAgentsJob' is disabled.");
        }
    }

}
