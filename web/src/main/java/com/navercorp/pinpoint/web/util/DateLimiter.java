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

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
@Component
public class DateLimiter implements Limiter {

    private final long limitDay;
    private final long limitDayMillis;

    public DateLimiter() {
        this(2);
    }

    public DateLimiter(int limitDay) {
        if (limitDay < 0) {
            throw new IllegalArgumentException("limitDay < 0 " + limitDay);
        }
        this.limitDay = limitDay;
        this.limitDayMillis = TimeUnit.DAYS.toMillis((long) limitDay);
    }

    @Override
    public void limit(long from, long to) {
        final long elapsedTime = to - from;
        if (elapsedTime < 0) {
            throw new  IllegalArgumentException("to - from < 0 from:" + from + " to:" + to);
        }
        if (limitDayMillis < elapsedTime) {
            throw new IllegalArgumentException("limitDay:"+ limitDay + " from:" + from + " to:" + to);
        }
    }

    @Override
    public void limit(Range range) {
        if (range == null) {
            throw new NullPointerException("range");
        }
        final long elapsedTime = range.getRange();
        if (elapsedTime < 0) {
            throw new  IllegalArgumentException("to - from < 0 " + range);
        }
        if (limitDayMillis < elapsedTime) {
            throw new IllegalArgumentException("limitDay:"+ limitDay + " " + range);
        }
    }
}
