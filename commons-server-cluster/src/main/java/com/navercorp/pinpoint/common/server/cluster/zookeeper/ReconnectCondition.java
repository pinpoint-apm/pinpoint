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

import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class ReconnectCondition {

    private static final long MIN_MAX_KEEP_NOT_CONNECTED_TIME_MILLIS = TimeUnit.MINUTES.toMillis(1);
    private static final long DEFAULT_MAX_KEEP_NOT_CONNECTED_TIME_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private static final int MIN_MAX_COUNT_OF_CONSECUTIVE_NOT_CONNECTED = (int) TimeUnit.MINUTES.toSeconds(1);
    private static final int DEFAULT_MAX_COUNT_OF_CONSECUTIVE_NOT_CONNECTED = (int) TimeUnit.MINUTES.toSeconds(5);


    private final long maxKeepNotConnectedTimeMillis;

    private final int maxCountOfConsecutiveNotConnected;

    public ReconnectCondition() {
        this(DEFAULT_MAX_KEEP_NOT_CONNECTED_TIME_MILLIS, DEFAULT_MAX_COUNT_OF_CONSECUTIVE_NOT_CONNECTED);
    }

    public ReconnectCondition(long maxKeepNotConnectedTimeMillis, int maxCountOfConsecutiveNotConnected) {
        if (maxKeepNotConnectedTimeMillis < MIN_MAX_KEEP_NOT_CONNECTED_TIME_MILLIS) {
            this.maxKeepNotConnectedTimeMillis = MIN_MAX_KEEP_NOT_CONNECTED_TIME_MILLIS;
        } else {
            this.maxKeepNotConnectedTimeMillis = maxKeepNotConnectedTimeMillis;
        }

        if (maxCountOfConsecutiveNotConnected < MIN_MAX_COUNT_OF_CONSECUTIVE_NOT_CONNECTED) {
            this.maxCountOfConsecutiveNotConnected = MIN_MAX_COUNT_OF_CONSECUTIVE_NOT_CONNECTED;
        } else {
            this.maxCountOfConsecutiveNotConnected = maxCountOfConsecutiveNotConnected;
        }
    }

    public boolean check(NotConnectedStatus notConnectedStatus) {
        if (notConnectedStatus.getKeepStatusStartTime() < maxKeepNotConnectedTimeMillis) {
            return false;
        }

        if (notConnectedStatus.getStatusCount() < maxCountOfConsecutiveNotConnected) {
            return false;
        }

        return true;
    }

}
