package com.navercorp.pinpoint.metric.common.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class TagComparatorTest {

    @Test
    public void compareTest() {
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("HHH", "1234"));
        tagList.add(new Tag("AAAA", "1123"));
        tagList.add(new Tag("ZZZZ", "1123"));
        tagList.add(new Tag("AABB", "1123"));
        tagList.add(new Tag("ACCC", "1123"));
        tagList.add(new Tag("AAAAAAAA", "1123"));

        Assertions.assertEquals(tagList.size(), 6);
        Assertions.assertEquals(tagList.get(0).getName(), "HHH");
        Assertions.assertEquals(tagList.get(1).getName(), "AAAA");
        Assertions.assertEquals(tagList.get(2).getName(), "ZZZZ");
        Assertions.assertEquals(tagList.get(3).getName(), "AABB");
        Assertions.assertEquals(tagList.get(4).getName(), "ACCC");
        Assertions.assertEquals(tagList.get(5).getName(), "AAAAAAAA");

        Comparator<Tag> comparator = new TagComparator();
        tagList.sort(comparator);

        Assertions.assertEquals(tagList.size(), 6);
        Assertions.assertEquals(tagList.get(0).getName(), "AAAA");
        Assertions.assertEquals(tagList.get(1).getName(), "AAAAAAAA");
        Assertions.assertEquals(tagList.get(2).getName(), "AABB");
        Assertions.assertEquals(tagList.get(3).getName(), "ACCC");
        Assertions.assertEquals(tagList.get(4).getName(), "HHH");
        Assertions.assertEquals(tagList.get(5).getName(), "ZZZZ");
    }
}

