package com.navercorp.pinpoint.metric.common.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author minwoo.jung
 */
public class TagComparatorTest {

    @Test
    public void compareTest() {
        List<Tag> tagList = Arrays.asList(
                new Tag("HHH", "1234"),
                new Tag("AAAA", "1123"),
                new Tag("ZZZZ", "1123"),
                new Tag("AABB", "1123"),
                new Tag("ACCC", "1123"),
                new Tag("AAAAAAAA", "1123")
        );

        assertThat(tagList)
                .hasSize(6)
                .map(Tag::getName)
                .containsExactly("HHH", "AAAA", "ZZZZ", "AABB", "ACCC", "AAAAAAAA");

        tagList.sort(TagComparator.INSTANCE);

        assertThat(tagList)
                .hasSize(6)
                .map(Tag::getName)
                .containsExactly("AAAA", "AAAAAAAA", "AABB", "ACCC", "HHH", "ZZZZ");
    }
}

