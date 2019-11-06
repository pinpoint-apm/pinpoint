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

/**
 * FIXME could just use Range..
 * 
 * @author netspider
 * 
 */
public final class ResponseTimeRange {
    private final int from;
    private final int to;

    public ResponseTimeRange(int from, int to) {
        this.from = from;
        this.to = to;
        validate();
    }

    public ResponseTimeRange(int from, int to, boolean check) {
        this.from = from;
        this.to = to;
        if (check) {
            validate();
        }
    }

    public static ResponseTimeRange createUncheckedRange(int from, int to) {
        return new ResponseTimeRange(from, to, false);
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getRange() {
        return to - from;
    }

    public void validate() {
        if (this.to < this.from) {
            throw new IllegalArgumentException("invalid range:" + this);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + from;
        result = prime * result + to;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResponseTimeRange other = (ResponseTimeRange) obj;
        if (from != other.from)
            return false;
        if (to != other.to)
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ResponseTimeRange{");
        sb.append("from=").append(from);
        sb.append(", to=").append(to);
        sb.append('}');
        return sb.toString();
    }
}
