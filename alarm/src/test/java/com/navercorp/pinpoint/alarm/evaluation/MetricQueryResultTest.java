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
