package com.navercorp.pinpoint.metric.web.service;

import com.navercorp.pinpoint.metric.common.model.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author minwoo.jung
 */
public class SystemMetricHostInfoServiceImplTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void test() {
        List<Tag> tagList = List.of(
                new Tag("key", "value")
        );

        List<Tag> comparedTagList = new ArrayList<>();

        assertThat(tagList).containsAll(comparedTagList);
    }
}