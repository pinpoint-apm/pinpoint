package com.navercorp.pinpoint.metric.collector.model;

import com.navercorp.pinpoint.metric.common.model.Tag;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author minwoo.jung
 */
public class SystemMetricJsonDeserializerTest {


    @Test
    public void test() {
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("바바바", "1234"));
        tagList.add(new Tag("가나가가", "1123"));
        tagList.add(new Tag("하하하", "1123"));
        tagList.add(new Tag("가가가", "1123"));
        tagList.add(new Tag("사사사", "1123"));
        tagList.add(new Tag("가가가가", "1123"));

        for (Tag tag : tagList) {
            System.out.println(tag);
        }
        Comparator<Tag> comparator = new Comparator<Tag>() {
            @Override
            public int compare(Tag o1, Tag o2) {
                return o1.getName().compareTo(o2.getName());
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }
        };
        tagList.sort(comparator);

        System.out.println("====================");

        for (Tag tag : tagList) {
            System.out.println(tag);
        }

        System.out.println("====================");
        List<Tag> tagList2 = new ArrayList<>();
        tagList2.add(new Tag("하하하2", "1123"));
        tagList2.add(new Tag("바바바2", "1234"));
        tagList2.add(new Tag("사사사2", "1123"));
        tagList2.add(new Tag("가가가2", "1123"));
        tagList2.add(new Tag("가나가가2", "1123"));
        tagList2.add(new Tag("가가가가2", "1123"));

        tagList2.sort(comparator);
        for (Tag tag : tagList2) {
            System.out.println(tag);
        }
    }

}