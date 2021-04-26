package com.navercorp.pinpoint.metric.common.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author minwoo.jung
 */
class TagComparatorTest {

    @Test
    void compareTest() {
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("HHH", "1234"));
        tagList.add(new Tag("AAAA", "1123"));
        tagList.add(new Tag("ZZZZ", "1123"));
        tagList.add(new Tag("AABB", "1123"));
        tagList.add(new Tag("ACCC", "1123"));
        tagList.add(new Tag("AAAAAAAA", "1123"));

        assertEquals(tagList.size(), 6);
        assertEquals(tagList.get(0).getName(), "HHH");
        assertEquals(tagList.get(1).getName(), "AAAA");
        assertEquals(tagList.get(2).getName(), "ZZZZ");
        assertEquals(tagList.get(3).getName(), "AABB");
        assertEquals(tagList.get(4).getName(), "ACCC");
        assertEquals(tagList.get(5).getName(), "AAAAAAAA");

        Comparator<Tag> comparator = new TagComparator();
        tagList.sort(comparator);

        assertEquals(tagList.size(), 6);
        assertEquals(tagList.get(0).getName(), "AAAA");
        assertEquals(tagList.get(1).getName(), "AAAAAAAA");
        assertEquals(tagList.get(2).getName(), "AABB");
        assertEquals(tagList.get(3).getName(), "ACCC");
        assertEquals(tagList.get(4).getName(), "HHH");
        assertEquals(tagList.get(5).getName(), "ZZZZ");
    }
}

