/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.alarm.core.service;

import com.navercorp.pinpoint.applicationmap.histogram.ApdexScore;
import com.navercorp.pinpoint.applicationmap.histogram.TimeHistogram;

/**
 * A window's response histograms added up.
 *
 * <p>Shared by the readers of the two map statistics, so that what a rule against an
 * application means by "slow" is what a rule against its calls to another means by it.
 *
 * <p>Slow and very slow are one bucket, the way the v1 alarm counted them: a rule asking
 * about slow responses means both.
 */
record ResponseSummary(long fastCount, long normalCount, long slowCount,
                       long errorCount, long totalCount) {

    static final ResponseSummary EMPTY = new ResponseSummary(0, 0, 0, 0, 0);

    static ResponseSummary of(Iterable<TimeHistogram> histograms) {
        long fast = 0;
        long normal = 0;
        long slow = 0;
        long error = 0;
        long total = 0;
        for (TimeHistogram histogram : histograms) {
            fast += histogram.getFastCount();
            normal += histogram.getNormalCount();
            slow += histogram.getSlowCount() + histogram.getVerySlowCount();
            error += histogram.getTotalErrorCount();
            total += histogram.getTotalCount();
        }
        return new ResponseSummary(fast, normal, slow, error, total);
    }

    /**
     * Taken over the whole window rather than averaged across its slots: a minute with one
     * slow request out of one is not fifty percent of an hour that had twenty.
     */
    double percentOf(long value) {
        if (totalCount == 0 || value == 0) {
            return 0;
        }
        return (value * 100.0) / totalCount;
    }

    /**
     * The metrics both map readers share, by the name a rule calls them. Here rather than in
     * either reader because the arithmetic is this record's, and a reader that offers more --
     * response adds apdex -- answers for that one itself and delegates the rest.
     *
     * @return null when the name is not one of these, which the caller reports as unknown
     */
    Double valueOf(String metric) {
        return switch (metric == null ? "" : metric) {
            case "slow_count" -> (double) slowCount();
            case "error_count" -> (double) errorCount();
            case "total_count" -> (double) totalCount();
            case "slow_rate" -> percentOf(slowCount());
            case "error_rate" -> percentOf(errorCount());
            default -> null;
        };
    }

    /**
     * Reported on the same 0-100 scale as the rates, so one threshold field fits both.
     * Satisfied is the fast bucket and tolerating the normal one, as in the v1 alarm.
     */
    double apdexScore() {
        return new ApdexScore(fastCount, normalCount, totalCount).getApdexScore() * 100.0;
    }
}
