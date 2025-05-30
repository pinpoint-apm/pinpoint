package com.navercorp.pinpoint.metric.web.util;

import com.navercorp.pinpoint.metric.common.model.Tag;

import java.util.List;

public class TagListUtils {

    public static boolean containsAll(List<Tag> list1, List<Tag> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }

        return list1.containsAll(list2);
    }
}
