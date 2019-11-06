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
 * Class representing the area selected in the scatter chart
 * 
 * @author netspider
 * 
 */
public final class SelectedScatterArea {

    private final Range timeRange;
    private final ResponseTimeRange responseTimeRange;

    public SelectedScatterArea(long timeFrom, long timeTo, int responseTimeFrom, int responseTimeTo) {
        this.timeRange = new Range(timeFrom, timeTo);
        this.responseTimeRange = new ResponseTimeRange(responseTimeFrom, responseTimeTo);
    }

    public SelectedScatterArea(long timeFrom, long timeTo, int responseTimeFrom, int responseTimeTo, boolean check) {
        this(timeFrom, timeTo, responseTimeFrom, responseTimeTo);
        if (check) {
            isValid();
        }
    }

    public static SelectedScatterArea createUncheckedArea(long timeFrom, long timeTo, int responseTimeFrom, int responseTimeTo) {
        return new SelectedScatterArea(timeFrom, timeTo, responseTimeFrom, responseTimeTo);
    }

    private void isValid() {
        timeRange.validate();
        responseTimeRange.validate();
    }

    public Range getTimeRange() {
        return timeRange;
    }

    public ResponseTimeRange getResponseTimeRange() {
        return responseTimeRange;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((responseTimeRange == null) ? 0 : responseTimeRange.hashCode());
        result = prime * result + ((timeRange == null) ? 0 : timeRange.hashCode());
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
        SelectedScatterArea other = (SelectedScatterArea) obj;
        if (responseTimeRange == null) {
            if (other.responseTimeRange != null)
                return false;
        } else if (!responseTimeRange.equals(other.responseTimeRange))
            return false;
        if (timeRange == null) {
            if (other.timeRange != null)
                return false;
        } else if (!timeRange.equals(other.timeRange))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SelectedScatterArea{");
        sb.append("timeRange=").append(timeRange);
        sb.append(", responseTimeRange=").append(responseTimeRange);
        sb.append('}');
        return sb.toString();
    }
}
