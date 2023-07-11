package com.navercorp.pinpoint.metric.common.model.mybatis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.common.model.json.Tags;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


public class TagListTypeHandlerTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void test() throws JsonProcessingException {
        ObjectMapper mapper = TagListTypeHandler.getMapper();

        List<Tag> list = List.of(
                new Tag("a", "1"),
                new Tag("a", "2")
        );

        String json = mapper.writeValueAsString(list);
        logger.debug("serialize:{}", json);

        Tags tags = mapper.readValue(json, Tags.class);
        logger.debug("deserialize:{}", tags.getTags());

        Assertions.assertEquals(list, tags.getTags());
    }
}