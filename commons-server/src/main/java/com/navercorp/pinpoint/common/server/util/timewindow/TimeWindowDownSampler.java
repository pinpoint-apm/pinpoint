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

package com.navercorp.pinpoint.common.server.util.timewindow;

import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
public class TimeWindowDownSampler implements TimeWindowSampler {

    private static final long ONE_MINUTE = 6000 * 10;
    private static final long ONE_HOUR = TimeUnit.HOURS.toMillis(1);
    private static final long SIX_HOURS = TimeUnit.HOURS.toMillis(6);
    private static final long TWELVE_HOURS = TimeUnit.HOURS.toMillis(12);
    private static final long ONE_DAY = TimeUnit.DAYS.toMillis(1);
    private static final long TWO_DAY = TimeUnit.DAYS.toMillis(2);


    public static final TimeWindowSampler SAMPLER = new TimeWindowDownSampler();

    @Override
    public long getWindowSize(final long durationMills) {
        if (durationMills <= ONE_HOUR) {
            return ONE_MINUTE;
        } else if (durationMills <= SIX_HOURS) {
            return ONE_MINUTE * 5;
        } else if (durationMills <= TWELVE_HOURS) {
            return ONE_MINUTE * 10;
        } else if (durationMills <= ONE_DAY) {
            return ONE_MINUTE * 20;
        } else if (durationMills <= TWO_DAY) {
            return ONE_MINUTE * 30;
        }
        return ONE_MINUTE * 60;
    }
}
