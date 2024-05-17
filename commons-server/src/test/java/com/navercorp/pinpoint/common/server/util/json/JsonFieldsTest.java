package com.navercorp.pinpoint.common.server.util.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

class JsonFieldsTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    ObjectMapper mapper = Jackson.newMapper();

    @Test
    void testObjectKey() throws Exception {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("key1-1", "value1");
        map.put("key2-2", "value2");
        String mapString = mapper.writeValueAsString(map);

        JsonFields.Builder<Key, String> builder = JsonFields.newBuilder(Key::name);
        builder.addField(new Key("key1", 1), "value1");
        builder.addField(new Key("key2", 2), "value2");
        JsonFields<Key, String> jsonObject = builder.build();

        String jsonFieldStr = mapper.writeValueAsString(jsonObject);

        Assertions.assertEquals(mapString, jsonFieldStr);
    }

    public record Key(String key, int value) {
        public String name() {
            return key + '-' + value;
        }
    }

    @Test
    void stringKey() throws Exception {

        Map<String, String> map = new LinkedHashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        String mapString = mapper.writeValueAsString(map);

        JsonFields.Builder<String, String> builder = JsonFields.newBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.addField(entry.getKey(), entry.getValue());
        }

        JsonFields<String, String> jsonObject = builder.build();
        String jsonFieldStr = mapper.writeValueAsString(jsonObject);

        Assertions.assertEquals(mapString, jsonFieldStr);
    }


    @Test
    void test_addField() throws Exception {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("key1", "value1");
        String mapString = mapper.writeValueAsString(map);

        JsonFields.Builder<String, String> builder = JsonFields.newBuilder();
        JsonFields<String, String> value = builder.addField(JsonField.of("key1", "value1"))
                .build();

        String jsonFieldStr = mapper.writeValueAsString(value);

        Assertions.assertEquals(mapString, jsonFieldStr);
    }

    @Test
    void testIterator() {
        JsonFields.Builder<String, String> builder = JsonFields.newBuilder();
        JsonFields<String, String> fields = builder.addField("k", "v")
                .build();

        JsonField<String, String> v1 = fields.iterator().next();
        Assertions.assertEquals("k", v1.name());
        Assertions.assertEquals("v", v1.value());
    }

    @Test
    void testSort() {
        JsonFields.Builder<Integer, Integer> builder = JsonFields.newBuilder();
        builder.comparator(Comparator.comparing(JsonField::name));

        JsonFields<Integer, Integer> fields = builder
                .addField(3, 3)
                .addField(2, 2)
                .addField(1, 1)
                .addField(0, 0)
                .build();

        for (int i = 0; i < fields.size(); i++) {
            JsonField<Integer, Integer> field = fields.get(i);
            Assertions.assertEquals(i, field.name());
        }
    }

    @Test
    void throwIfDuplicateKeys()  {
        JsonFields.Builder<String, String> builder = JsonFields.newBuilder();
        builder.throwIfDuplicateKeys(true);

        builder.addField("a", "1");

        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.addField("a", "2"));
    }

    @Test
    void jsonNodeFactory_sample() throws Exception {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode jsonNodes = factory.objectNode();
        jsonNodes.set("string", factory.pojoNode("value"));
        jsonNodes.put("int", 1);

        String json = mapper.writeValueAsString(jsonNodes);
        logger.debug("json {}", json);
    }


}