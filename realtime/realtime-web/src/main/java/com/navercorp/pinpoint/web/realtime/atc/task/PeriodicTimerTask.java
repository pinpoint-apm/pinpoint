/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.web.realtime.atc.task;

import com.navercorp.pinpoint.util.ScheduleUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author youngjin.kim2
 */
public abstract class PeriodicTimerTask extends TimerTask implements InitializingBean {

    private final Logger logger;
    private final ScheduledExecutorService executor;
    private final long periodMs;

    public PeriodicTimerTask(Logger logger, String name, long periodMs) {
        this.logger = logger;
        this.executor = ScheduleUtil.makeScheduledExecutorService(name);
        this.periodMs = periodMs;
    }

    protected abstract void runPeriodicTask();

    @Override
    public void run() {
        try {
            runPeriodicTask();
        } catch (Exception e) {
            this.logger.error("runPeriodicTask", e);
        }
        scheduleNext();
    }

    @Override
    public void afterPropertiesSet() {
        scheduleNext();
    }

    private void scheduleNext() {
        executor.schedule(this, this.periodMs, TimeUnit.MILLISECONDS);
    }

}
