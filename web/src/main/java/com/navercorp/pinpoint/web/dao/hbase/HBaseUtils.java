package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class HBaseUtils {
    private HBaseUtils() {
    }

    private static <T> List<T> nonNullList(T... elements) {
        if (ArrayUtils.isEmpty(elements)) {
            return Collections.emptyList();
        }

        List<T> list = new ArrayList<>(elements.length);
        for (T ele : elements) {
            if (ele != null) {
                list.add(ele);
            }
        }
        return list;
    }

    public static Filter newFilterList(Filter... filters) {
        List<Filter> nonNullFilters = nonNullList(filters);
        if (CollectionUtils.isEmpty(nonNullFilters)) {
            return null;
        }
        return new FilterList(nonNullFilters);
    }

    public static int getScanCacheSize(Range range, long timespan, int maxCacheSize) {
        Objects.requireNonNull(range, "range");

        long scanRange = range.durationMillis();
        long expectedNumRows = ((scanRange - 1) / timespan) + 1;
        if (range.getFrom() != AgentStatUtils.getBaseTimestamp(range.getFrom())) {
            expectedNumRows++;
        }
        if (expectedNumRows > maxCacheSize) {
            return maxCacheSize;
        } else {
            // expectedNumRows guaranteed to be within integer range at this point
            return (int) expectedNumRows;
        }
    }

}
