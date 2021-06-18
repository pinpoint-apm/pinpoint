package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HBaseUtils {

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
}
