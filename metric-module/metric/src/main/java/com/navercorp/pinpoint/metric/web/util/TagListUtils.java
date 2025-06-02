package com.navercorp.pinpoint.metric.web.util;

import com.navercorp.pinpoint.metric.common.model.Tag;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class TagListUtils {

    public static boolean containsAll(List<Tag> list1, List<Tag> list2) {
        return CollectionUtils.isEqualCollection(list1, list2);
    }
}
