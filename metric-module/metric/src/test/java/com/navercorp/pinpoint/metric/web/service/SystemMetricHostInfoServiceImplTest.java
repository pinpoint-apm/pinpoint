package com.navercorp.pinpoint.metric.web.service;

import com.navercorp.pinpoint.metric.common.model.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class SystemMetricHostInfoServiceImplTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void test() {
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("key", "value"));

        List<Tag> comparedTagList = new ArrayList<>();

        if (tagList.containsAll(comparedTagList)) {
            logger.debug("success");
        } else {
            Assert.fail();
        }

    }
}