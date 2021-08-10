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

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.InitializingBean;

import java.util.Objects;

/**
 * @author minwoo.jung<minwoo.jung@navercorp.com>
 *
 */
public class JobLaunchSupport implements InitializingBean {

    private final JobLocator locator;
    private final JobLauncher launcher;

    public JobLaunchSupport(JobLocator locator, JobLauncher launcher) {
        this.locator = Objects.requireNonNull(locator, "locator");
        this.launcher = Objects.requireNonNull(launcher, "launcher");
    }

    public JobExecution run(String jobName, JobParameters params) {
        try {
            Job job = locator.getJob(jobName);
            return launcher.run(job, params);
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
