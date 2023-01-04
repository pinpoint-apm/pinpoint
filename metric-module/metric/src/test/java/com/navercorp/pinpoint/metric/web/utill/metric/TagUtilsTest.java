package com.navercorp.pinpoint.metric.web.utill.metric;

import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.util.TagUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TagUtilsTest {

    @Test
    public void parseTagTest() {
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("A", "1"));
        tagList.add(new Tag("B", "2"));
        tagList.add(new Tag("C", "3"));

        List<Tag> result = new ArrayList<>();
        result.add(TagUtils.parseTag("A:1"));
        result.add(TagUtils.parseTag("B:2"));
        result.add(TagUtils.parseTag("C:3"));

        Assertions.assertEquals(tagList, result);
    }

    @Test
    public void parseTagsListTest() {
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("A", "1"));
        tagList.add(new Tag("B", "2"));
        tagList.add(new Tag("C", "3"));

        List<String> tags = new ArrayList<>();
        tags.add("A:1");
        tags.add("B:2");
        tags.add("C:3");

        List<Tag> result = TagUtils.parseTags(tags);

        Assertions.assertEquals(tagList, result);
    }


    @Test
    public void parseTagsStringParameterTest() {
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("device", "1"));
        tagList.add(new Tag("fstype", "2"));
        tagList.add(new Tag("mode", "3"));
        tagList.add(new Tag("path", "4"));

        String parameterTagsString = "device:1,fstype:2,mode:3,path:4";
        List<Tag> result = TagUtils.parseTags(parameterTagsString);

        Assertions.assertEquals(tagList, result);
    }

    @Test
    public void parseTagsStringNullParameterTest() {
        String nullString = null;
        List<Tag> result = TagUtils.parseTags(nullString);

        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void parseTagsStringListTypeTest() {
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("device", "1"));
        tagList.add(new Tag("fstype", "2"));
        tagList.add(new Tag("mode", "3"));
        tagList.add(new Tag("path", "4"));

        String multiValueFieldTagString = "\"device:1\",\"fstype:2\",\"mode:3\",\"path:4\"";
        List<Tag> result = TagUtils.parseTags(multiValueFieldTagString);

        Assertions.assertEquals(tagList, result);
    }

    @Test
    public void parseTagsStringListTypeNullTest() {
        String multiValueFieldTagString = "\"null\"";
        List<Tag> result = TagUtils.parseTags(multiValueFieldTagString);

        Assertions.assertEquals(0, result.size());
    }


    @Test
    public void toTagsStringTest() {
        String jsonTagString = "{\"device\":\"1\",\"fstype\":\"2\",\"mode\":\"3\",\"path\":\"4\"}";
        String result = TagUtils.toTagString(jsonTagString);

        Assertions.assertEquals("device:1,fstype:2,mode:3,path:4", result);
    }

    @Test
    public void toTagsStringEmptyTest() {
        String jsonTagString = "{}";
        String result = TagUtils.toTagString(jsonTagString);

        Assertions.assertEquals("", result);
    }
}
