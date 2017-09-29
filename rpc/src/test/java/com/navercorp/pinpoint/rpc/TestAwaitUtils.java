/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.rpc;

import com.navercorp.pinpoint.common.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class TestAwaitUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(TestAwaitUtils.class);

    private final long waitUnitTime;
    private final long maxWaitTime;

    public TestAwaitUtils(long waitUnitTime, long maxWaitTime) {
        this.waitUnitTime = waitUnitTime;
        this.maxWaitTime = maxWaitTime;
    }

    public boolean await(TestAwaitTaskUtils awaitTaskUtils) {
        return await(awaitTaskUtils, waitUnitTime, maxWaitTime);
    }

    public static boolean await(TestAwaitTaskUtils awaitTaskUtils, long waitUnitTime, long maxWaitTime) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        while (true) {
            try {
                if (awaitTaskUtils.checkCompleted()) {
                    return true;
                }
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }

            try {
                Thread.sleep(waitUnitTime);
            } catch (InterruptedException e) {
            }

            if (stopWatch.stop() > maxWaitTime) {
                return false;
            }
        }
    }

}
