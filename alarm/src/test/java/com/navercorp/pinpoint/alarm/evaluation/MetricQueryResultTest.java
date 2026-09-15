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
package com.navercorp.pinpoint.alarm.evaluation;

import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult.QueriedRange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricQueryResultTest {

    /**
     * Leaf order is the user's condition tree order, so the accumulator has to widen in both
     * directions. Going through a query service cannot show this: every leaf of a rule shares
     * one clock read, so the two ranges differ only in {@code fromMs}.
     */
    @Test
    void union_widensWhicheverSideIsNarrower() {
        QueriedRange wide = new QueriedRange(500L, 2000L);
        QueriedRange narrow = new QueriedRange(1000L, 2000L);

        assertEquals(wide, QueriedRange.union(narrow, wide));
        assertEquals(wide, QueriedRange.union(wide, narrow));
    }

    @Test
    void union_takesTheLatestEnd() {
        assertEquals(new QueriedRange(500L, 3000L),
                QueriedRange.union(new QueriedRange(500L, 2000L), new QueriedRange(1500L, 3000L)));
    }

    @Test
    void union_startsFromTheFirstQueriedRange() {
        QueriedRange range = new QueriedRange(500L, 2000L);

        assertEquals(range, QueriedRange.union(null, range));
    }
}
