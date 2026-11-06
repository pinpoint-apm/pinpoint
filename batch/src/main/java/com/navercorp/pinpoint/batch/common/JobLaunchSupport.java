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

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.InitializingBean;

import java.util.Objects;

/**
 * @author minwoo.jung<minwoo.jung@navercorp.com>
 *
 */
public class JobLaunchSupport implements InitializingBean {

    private final JobRegistry locator;
    private final JobOperator launcher;

    public JobLaunchSupport(JobRegistry locator, JobOperator launcher) {
        this.locator = Objects.requireNonNull(locator, "locator");
        this.launcher = Objects.requireNonNull(launcher, "launcher");
    }

    public JobExecution run(String jobName, JobParameters params) {
        try {
            Job job = locator.getJob(jobName);
            return launcher.start(job, params);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Objects.requireNonNull(locator, "jobLocator name");
        Objects.requireNonNull(launcher, "jobLauncher name");
    }
}
