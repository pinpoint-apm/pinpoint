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

package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.web.vo.Range;

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
    public long getWindowSize(Range range) {
        final long diff = range.getRange();
        long size;
        if (diff <= ONE_HOUR) {
            size = ONE_MINUTE;
        } else if (diff <= SIX_HOURS) {
            size = ONE_MINUTE * 5;
        } else if (diff <= TWELVE_HOURS) {
            size = ONE_MINUTE * 10;
        } else if (diff <= ONE_DAY) {
            size = ONE_MINUTE * 20;
        } else if (diff <= TWO_DAY) {
            size = ONE_MINUTE * 30;
        } else {
            size = ONE_MINUTE * 60;
        }

        return size;
    }
}
