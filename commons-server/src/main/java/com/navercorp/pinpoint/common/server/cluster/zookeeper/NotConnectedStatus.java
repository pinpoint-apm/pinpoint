/*
 * Copyright 2021 NAVER Corp.
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
 */

package com.navercorp.pinpoint.common.server.cluster.zookeeper;

/**
 * @author Taejin Koo
 */
public class NotConnectedStatus {

    private static long DEFAULT_START_TIME = Long.MAX_VALUE;

    private long statusStartTime = DEFAULT_START_TIME;
    private int statusCount = 0;

    void update() {
        if (statusStartTime == DEFAULT_START_TIME) {
            statusStartTime = System.currentTimeMillis();
        }

        statusCount++;
    }

    void reset() {
        statusStartTime = DEFAULT_START_TIME;
        statusCount = 0;
    }

    public long getKeepStatusStartTime() {
        if (statusStartTime == DEFAULT_START_TIME) {
            return -1;
        }

        return System.currentTimeMillis() - statusStartTime;
    }

    public int getStatusCount() {
        return statusCount;
    }

}
