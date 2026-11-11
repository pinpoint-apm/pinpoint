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

import com.google.common.util.concurrent.MoreExecutors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.SmartLifecycle;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Launches the startup jobs once, started and stopped by the context as a {@link SmartLifecycle}.
 * <p>
 * {@link #start()} runs after all singletons are ready, so a job does not start while the beans
 * it uses are still being initialized. The default phase is kept: the launcher starts last and
 * stops first, before the beans the jobs use are destroyed.
 *
 * @author youngjin.kim2
 */
public class StartupJobLauncher implements SmartLifecycle {

    private static final Logger logger = LogManager.getLogger(StartupJobLauncher.class);

    private final BatchJobLauncher launcher;
    private final List<String> jobs;

    private ExecutorService executor;
    private volatile boolean running;

    public StartupJobLauncher(BatchJobLauncher launcher, List<String> jobs) {
        this.launcher = Objects.requireNonNull(launcher, "launcher");
        this.jobs = Objects.requireNonNull(jobs, "jobs");
    }

    @Override
    public void start() {
        if (this.running) {
            return;
        }
        if (this.jobs.isEmpty()) {
            logger.info("No startup jobs to launch");
            return;
        }

        logger.info("Startup job launcher started");
        this.executor = Executors.newSingleThreadExecutor();
        this.executor.execute(() -> {
            for (String job : jobs) {
                logger.info("Launching job {}", job);
                launcher.run(job, BatchJobLauncher.createTimeParameter());
            }
            logger.info("Startup job launcher finished");
        });
        this.running = true;
    }

    @Override
    public void stop() {
        final ExecutorService executor = this.executor;
        if (executor != null) {
            MoreExecutors.shutdownAndAwaitTermination(executor, Duration.ofSeconds(3));
            this.executor = null;
        }
        this.running = false;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }
}
