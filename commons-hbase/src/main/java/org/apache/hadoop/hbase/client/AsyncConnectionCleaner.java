/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.hadoop.hbase.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AsyncConnectionCleaner {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public void clean() {
        try {
            Field retryTimerField = AsyncConnectionImpl.class.getDeclaredField("RETRY_TIMER");
            Object timer = retryTimerField.get(AsyncConnectionImpl.class);
            if (timer != null) {
                Method stop = timer.getClass().getMethod("stop");
                stop.invoke(timer);
                logger.info("Stopped retry timer: {}", timer);
            } else {
                logger.info("Retry timer is null");
            }
        } catch (Throwable e) {
            logger.warn("Failed to stop retry timer", e);
        }
    }
}
