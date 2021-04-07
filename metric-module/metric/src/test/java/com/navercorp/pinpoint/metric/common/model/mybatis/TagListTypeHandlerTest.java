package com.navercorp.pinpoint.metric.common.model.mybatis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.json.Tags;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class TagListTypeHandlerTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void test() throws JsonProcessingException {
        ObjectMapper mapper = TagListTypeHandler.createObjectMapper();

        List<Tag> list  = new ArrayList<>();
        list.add(new Tag("a", "1"));
        list.add(new Tag("a", "2"));

        String json = mapper.writeValueAsString(list);
        logger.debug("serialize:{}", json);

        Tags tags = mapper.readValue(json, Tags.class);
        logger.debug("deserialize:{}", tags.getTags());

        Assert.assertEquals(list, tags.getTags());
    }
}