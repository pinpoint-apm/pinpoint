/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.batch.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author youngjin.kim2
 */
public class StartupJobLauncher implements InitializingBean, DisposableBean {

    private static final Logger logger = LogManager.getLogger(StartupJobLauncher.class);

    private final BatchJobLauncher launcher;
    private final List<String> jobs;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public StartupJobLauncher(BatchJobLauncher launcher, List<String> jobs) {
        this.launcher = Objects.requireNonNull(launcher, "launcher");
        this.jobs = Objects.requireNonNull(jobs, "jobs");
    }

    @Override
    public void afterPropertiesSet() {
        if (this.jobs.isEmpty()) {
            logger.info("No startup jobs to launch");
            return;
        }

        logger.info("Startup job launcher started");
        this.executor.execute(() -> {
            for (String job : jobs) {
                logger.info("Launching job {}", job);
                launcher.run(job, BatchJobLauncher.createTimeParameter());
            }
            logger.info("Startup job launcher finished");
        });
    }

    @Override
    public void destroy() {
        this.executor.shutdownNow();
    }
}
