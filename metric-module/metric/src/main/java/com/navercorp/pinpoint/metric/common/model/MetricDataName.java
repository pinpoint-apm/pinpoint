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

package com.navercorp.pinpoint.metric.common.model;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;

import java.util.Calendar;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class MetricDataName {

    private final String fieldName;
    private final String metricName;
    private final long saveTime;

    public MetricDataName(String metricName, String fieldName) {
        this.metricName = StringPrecondition.requireHasLength(metricName, "metricName");
        this.fieldName = StringPrecondition.requireHasLength(fieldName, "fieldName");
        this.saveTime = createSaveTime();
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getMetricName() {
        return metricName;
    }

    public long getSaveTime() {
        return saveTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricDataName that = (MetricDataName) o;
        return saveTime == that.saveTime && fieldName.equals(that.fieldName) && metricName.equals(that.metricName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, metricName, saveTime);
    }

    public static long createSaveTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        return calendar.getTimeInMillis();
    }
}
