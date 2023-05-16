package com.navercorp.pinpoint.metric.web.utill.metric;

import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.util.TagUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TagUtilsTest {

    @Test
    public void parseTagTest() {
        List<Tag> tagList = List.of(
                new Tag("A", "1"),
                new Tag("B", "2"),
                new Tag("C", "3"));

        List<Tag> result = List.of(
                TagUtils.parseTag("A:1"),
                TagUtils.parseTag("B:2"),
                TagUtils.parseTag("C:3"));

        Assertions.assertEquals(tagList, result);
    }

    @Test
    public void parseTagsListTest() {
        List<Tag> tagList = List.of(
                new Tag("A", "1"),
                new Tag("B", "2"),
                new Tag("C", "3")
        );

        List<String> tags = List.of(
                "A:1",
                "B:2",
                "C:3"
        );

        List<Tag> result = TagUtils.parseTags(tags);

        Assertions.assertEquals(tagList, result);
    }


    @Test
    public void parseTagsStringParameterTest() {
        List<Tag> tagList = List.of(
                new Tag("device", "1"),
                new Tag("fstype", "2"),
                new Tag("mode", "3"),
                new Tag("path", "4")
        );

        String parameterTagsString = "device:1,fstype:2,mode:3,path:4";
        List<Tag> result = TagUtils.parseTags(parameterTagsString);

        Assertions.assertEquals(tagList, result);
    }

    @Test
    public void parseTagsStringNullParameterTest() {
        String nullString = null;
        List<Tag> result = TagUtils.parseTags(nullString);

        assertThat(result).isEmpty();
    }

    @Test
    public void parseTagsStringListTypeTest() {
        List<Tag> tagList = List.of(
                new Tag("device", "1"),
                new Tag("fstype", "2"),
                new Tag("mode", "3"),
                new Tag("path", "4")
        );

        String multiValueFieldTagString = "\"device:1\",\"fstype:2\",\"mode:3\",\"path:4\"";
        List<Tag> result = TagUtils.parseTags(multiValueFieldTagString);

        Assertions.assertEquals(tagList, result);
    }

    @Test
    public void parseTagsStringListTypeNullTest() {
        String multiValueFieldTagString = "\"null\"";
        List<Tag> result = TagUtils.parseTags(multiValueFieldTagString);

        assertThat(result).isEmpty();
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

        assertThat(result).isBlank();
    }
}
