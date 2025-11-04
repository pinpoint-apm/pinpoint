/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.service;

import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;

public class AlertViewService {

    public static final double DEFAULT_THRESHOLD = 10.0;

    private final double threshold;

    public AlertViewService() {
        this(DEFAULT_THRESHOLD);
    }

    public AlertViewService(double threshold) {
        this.threshold = threshold;
    }

    public boolean hasAlert(Histogram histogram) {
        return hasAlert(histogram.getTotalCount(), histogram.getTotalErrorCount());
    }

    public boolean hasAlert(long totalCount, long totalErrorCount) {
        double errorRate = getErrorRate(totalCount, totalErrorCount);
        if (errorRate == 0) {
            return false;
        }
        return errorRate >= threshold;
    }

    public double getErrorRate(long totalCount, long errorCount) {
        if (totalCount == 0) {
            return 0;
        }
        return ((double) errorCount / totalCount) * 100;
    }

}
