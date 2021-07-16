package com.navercorp.pinpoint.metric.common.model;

import org.junit.Assert;
import org.junit.Test;

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

        Assert.assertEquals(tagList.size(), 6);
        Assert.assertEquals(tagList.get(0).getName(), "HHH");
        Assert.assertEquals(tagList.get(1).getName(), "AAAA");
        Assert.assertEquals(tagList.get(2).getName(), "ZZZZ");
        Assert.assertEquals(tagList.get(3).getName(), "AABB");
        Assert.assertEquals(tagList.get(4).getName(), "ACCC");
        Assert.assertEquals(tagList.get(5).getName(), "AAAAAAAA");

        Comparator<Tag> comparator = new TagComparator();
        tagList.sort(comparator);

        Assert.assertEquals(tagList.size(), 6);
        Assert.assertEquals(tagList.get(0).getName(), "AAAA");
        Assert.assertEquals(tagList.get(1).getName(), "AAAAAAAA");
        Assert.assertEquals(tagList.get(2).getName(), "AABB");
        Assert.assertEquals(tagList.get(3).getName(), "ACCC");
        Assert.assertEquals(tagList.get(4).getName(), "HHH");
        Assert.assertEquals(tagList.get(5).getName(), "ZZZZ");
    }
}

