/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor;

import com.navercorp.pinpoint.common.util.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.List;

/**
 * @author Taejin Koo
 */
class StatMonitorJob implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();


    private final Runnable[] runnableList;

    public StatMonitorJob(List<Runnable> runnableList) {
        Assert.requireNonNull(runnableList, "runnableList");
        this.runnableList = runnableList.toArray(new Runnable[0]);
    }

    @Override
    public void run() {
        if (isDebug) {
            logger.debug("StatMonitorJob started. jobSize={}", runnableList.length);
        }

        for (Runnable runnable : runnableList) {
            runnable.run();
        }
    }

    public void close() {
        for (Runnable runnable : runnableList) {
            if (runnable instanceof Closeable) {
                try {
                    ((Closeable) runnable).close();
                } catch (Exception e) {
                }
            }
        }
    }

}
