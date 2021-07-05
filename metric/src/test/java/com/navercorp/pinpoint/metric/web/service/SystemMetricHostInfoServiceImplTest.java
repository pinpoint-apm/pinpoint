package com.navercorp.pinpoint.metric.web.service;

import com.navercorp.pinpoint.metric.common.model.Tag;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author minwoo.jung
 */
public class SystemMetricHostInfoServiceImplTest {

    @Test
    public void test() {
        List<Tag> tagList = new ArrayList<>(1);
        tagList.add(new Tag("key", "value"));
        List<Tag> comparedTagList = new ArrayList<>(1);

        if (tagList.containsAll(comparedTagList)) {
            System.out.println("success");
        } else {
            Assert.fail();
        }

    }
}