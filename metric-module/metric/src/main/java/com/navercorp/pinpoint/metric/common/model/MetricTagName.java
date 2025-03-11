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

/**
 * @author minwoo.jung
 */
public class MetricTagName {

    private final String applicationName;
    private final String metricName;
    private final String fieldName;

    public MetricTagName(String applicationName, String metricName, String fieldName) {
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.metricName = StringPrecondition.requireHasLength(metricName, "metricName");
        this.fieldName = StringPrecondition.requireHasLength(fieldName, "fieldName");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        MetricTagName that = (MetricTagName) o;
        return applicationName.equals(that.applicationName) && metricName.equals(that.metricName) && fieldName.equals(that.fieldName);
    }

    @Override
    public int hashCode() {
        int result = applicationName.hashCode();
        result = 31 * result + metricName.hashCode();
        result = 31 * result + fieldName.hashCode();
        return result;
    }
}
