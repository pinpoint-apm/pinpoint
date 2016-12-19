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

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.util.DateUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 * @author netspider
 */
public final class Range {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final long from;
    private final long to;

    public Range(long from, long to) {
        this.from = from;
        this.to = to;
        validate();
    }

    public Range(long from, long to, boolean check) {
        this.from = from;
        this.to = to;
        if (check) {
            validate();
        }
    }

    public static Range createUncheckedRange(long from, long to) {
        return new Range(from, to, false);
    }

    public long getFrom() {
        return from;
    }

    public String getFromDateTime() {
        return DateUtils.longToDateStr(from, DATE_TIME_FORMAT);
    }

    public long getTo() {
        return to;
    }

    public String getToDateTime() {
        return DateUtils.longToDateStr(to, DATE_TIME_FORMAT);
    }

    public long getRange() {
        return to - from;
    }

    public void validate() {
        if (this.to < this.from) {
            throw new IllegalArgumentException("invalid range:" + this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Range range = (Range) o;

        if (from != range.from) return false;
        if (to != range.to) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (from ^ (from >>> 32));
        result = 31 * result + (int) (to ^ (to >>> 32));
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Range{");
        sb.append("from=").append(from);
        sb.append(", to=").append(to);
        sb.append(", range=").append(getRange());
        sb.append('}');
        return sb.toString();
    }

    public String prettyToString() {
        final StringBuilder sb = new StringBuilder("Range{");
        sb.append("from=").append(from);
        sb.append(", to=").append(to);
        sb.append(", range s=").append(TimeUnit.MILLISECONDS.toSeconds(getRange()));
        sb.append('}');
        return sb.toString();
    }
}
