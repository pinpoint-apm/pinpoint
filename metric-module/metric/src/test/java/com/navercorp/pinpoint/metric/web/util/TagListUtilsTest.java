package com.navercorp.pinpoint.metric.web.util;

import com.navercorp.pinpoint.metric.common.model.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

class TagListUtilsTest {


    @Test
    void isEqualsUtils_same() {
        Tag t1 = new Tag("key1", "value");
        Tag t2 = new Tag("key2", "value");
        List<Tag> tags1 = List.of(t1, t2);
        List<Tag> tags2 = List.of(t2, t1);

        Assertions.assertTrue(TagListUtils.containsAll(tags1, tags2));
        Assertions.assertTrue(isEqualsUtils(tags1, tags2));
    }

    @Test
    void isEquals_cardinality() {
        Tag t1 = new Tag("key1", "value");
        Tag t2 = new Tag("key2", "value");
        List<Tag> tags1 = List.of(t1, t2, t2);
        List<Tag> tags2 = List.of(t2, t1, t1);

        Assertions.assertTrue(TagListUtils.containsAll(tags1, tags2));
        Assertions.assertTrue(isEqualsUtils(tags1, tags2));
    }

    @Test
    @Disabled
    void isEquals_containsall() {
        Tag t1 = new Tag("key1", "value");
        Tag t2 = new Tag("key2", "value");
        List<Tag> tags1 = List.of(t1, t2);
        List<Tag> tags2 = List.of(t2, t1, t1);

        Assertions.assertFalse(TagListUtils.containsAll(tags1, tags2));
        // list.size() != list2.size() issue
        Assertions.assertFalse(isEqualsUtils(tags1, tags2));
    }

    boolean isEqualsUtils(List<Tag> tagList1, List<Tag> tagList2) {
        // same cardinalities
//        return CollectionUtils.isEqualCollection(tagList1, tagList2);
        // sub set
//        return CollectionUtils.isSubCollection(tagList1, tagList2);
        return CollectionUtils.containsAll(tagList1, tagList2);
    }
}