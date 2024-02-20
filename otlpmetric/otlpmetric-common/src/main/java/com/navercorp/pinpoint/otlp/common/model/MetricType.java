/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.common.model;

public enum MetricType {
    GAUGE(0),
    SUM(1),
    HISTOGRAM(2),
    EXP_HISTOGRAM(3),
    SUMMARY(4);

    private final int value;
    public final int getNumber() {
        return this.value;
    }
    public static MetricType forNumber(int value) {
        switch (value) {
            case 0:
                return GAUGE;
            case 1:
                return SUM;
            case 2:
                return HISTOGRAM;
            case 3:
                return EXP_HISTOGRAM;
            case 4:
                return SUMMARY;
            default:
                return null;
        }
    }

    private MetricType(int value) {
        this.value = value;
    }
}
