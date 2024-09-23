package com.navercorp.pinpoint.metric.common.mybatis.typehandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.metric.common.model.Tag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


class TagListSerializerTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void serialize() {

        TagListSerializer serializer = new TagListSerializer(mapper);

        List<Tag> tagList = List.of(new Tag("name1", "1"), new Tag("name2", "2"));

        String json = serializer.serialize(tagList);

        List<Tag> newTagList = serializer.deserialize(json);

        Assertions.assertEquals(tagList, newTagList);

    }
}