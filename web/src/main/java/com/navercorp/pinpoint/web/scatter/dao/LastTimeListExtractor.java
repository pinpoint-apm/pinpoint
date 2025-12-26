package com.navercorp.pinpoint.web.scatter.dao;

import com.navercorp.pinpoint.common.hbase.LastRowHandler;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.ToLongFunction;

public class LastTimeListExtractor {
    private static final long DEFAULT_NULL = -1;
    private final long nullTimestamp;

    public LastTimeListExtractor() {
        this(DEFAULT_NULL);
    }

    public LastTimeListExtractor(long nullTimestamp) {
        this.nullTimestamp = nullTimestamp;
    }


    public <T> long getLastTime(LastRowHandler<List<T>> lastRowAccessor, ToLongFunction<T> timestampExtractor) {
        List<T> lastRow = lastRowAccessor.getLastRow();
        if (CollectionUtils.isEmpty(lastRow)) {
            return nullTimestamp;
        }
        T last = CollectionUtils.lastElement(lastRow);
        if (last == null) {
            return nullTimestamp;
        }
        return timestampExtractor.applyAsLong(last);
    }

    static LastTimeListExtractor lastTimeExtractor = new LastTimeListExtractor();

    public static <T> long getLastTime(boolean overflow, LastRowHandler<List<T>> lastRowAccessor, ToLongFunction<T> timestampExtractor,
                                       long fallbackLastIndex) {
        if (overflow) {
            return lastTimeExtractor.getLastTime(lastRowAccessor, timestampExtractor);
        } else {
            return fallbackLastIndex;
        }
    }

    public static boolean isOverflow(List<?> values, int limit) {
        return values.size() >= limit;
    }


}
