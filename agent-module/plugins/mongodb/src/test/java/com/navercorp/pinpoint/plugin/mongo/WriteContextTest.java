package com.navercorp.pinpoint.plugin.mongo;

import com.mongodb.client.model.Filters;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.bson.BsonBinary;
import org.bson.types.ObjectId;
import org.bson.BsonString;
import org.bson.BsonObjectId;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class WriteContextTest {

    @Test
    void parse_binary() {
        List<String> parameter = new ArrayList<>();
        WriteContext context = new WriteContext(parameter, true, true);

        BsonDocument bson = new BsonDocument();
        BsonBinary binary = new BsonBinary("123456789".getBytes(StandardCharsets.UTF_8));
        bson.append("bson", binary);

        context.parse(bson);
        
        String input = parameter.get(0);
        String nopadInput = input.substring(1, WriteContext.DEFAULT_ABBREVIATE_MAX_WIDTH + 1);

        byte[] bytes = "12345678".getBytes(StandardCharsets.UTF_8);
        byte[] sourceByes = Arrays.copyOf(Base64.getEncoder().encode(bytes), WriteContext.DEFAULT_ABBREVIATE_MAX_WIDTH);
        Assertions.assertEquals(BytesUtils.toString(sourceByes), nopadInput);
    }


    @Test
    void parse_binary_subtype() {
        assertBinarySubtype((byte) 0x00, "00");
        assertBinarySubtype((byte) 0x04, "04");
        // user defined subtypes are negative as a byte
        assertBinarySubtype((byte) 0x80, "80");
        assertBinarySubtype((byte) 0xFF, "FF");
    }

    private static void assertBinarySubtype(byte subtype, String expected) {
        List<String> parameter = new ArrayList<>();
        WriteContext context = new WriteContext(parameter, true, true);
        BsonDocument bson = new BsonDocument();
        bson.append("bson", new BsonBinary(subtype, "1234".getBytes(StandardCharsets.UTF_8)));
        context.parse(bson);

        // the binary value is bound as the first parameter and the "$type" hex as the second
        Assertions.assertEquals(2, parameter.size());
        Assertions.assertEquals("\"" + expected + "\"", parameter.get(1));
    }

    @Test
    void parse_objectId_stringToken() {
        List<String> parameter = new ArrayList<>();
        WriteContext context = new WriteContext(parameter, true, true);
        ObjectId objectId = new ObjectId();
        BsonDocument bson = new BsonDocument();
        // "_id" is skipped by writeBsonObject, bind the ObjectId under another key
        bson.append("ref", new BsonObjectId(objectId));

        context.parse(bson);

        Assertions.assertEquals("\"" + objectId.toHexString() + "\"", parameter.get(0));
    }

    @Test
    void parse_string_escaped_and_abbreviated() {
        List<String> parameter = new ArrayList<>();
        WriteContext context = new WriteContext(parameter, true, true);
        StringBuilder longValue = new StringBuilder("say \"hi\"");
        for (int i = 0; i < 100; i++) {
            longValue.append('x');
        }
        BsonDocument bson = new BsonDocument();
        bson.append("name", new BsonString(longValue.toString()));

        context.parse(bson);

        String bound = parameter.get(0);
        Assertions.assertTrue(bound.startsWith("\"say \"\"hi\"\""), bound);
        Assertions.assertTrue(bound.length() < longValue.length(), bound);
    }

    @Test
    void parse_geometry() {
        List<String> parameter = new ArrayList<>();
        WriteContext context = new WriteContext(parameter, true, true);

        BsonDocument bson = new BsonDocument();
        BsonBinary binary = new BsonBinary("123456789".getBytes(StandardCharsets.UTF_8));
        bson.append("bson", binary);

        Bson geo = Filters.geoIntersects("geo", bson);
        // touch GeometryOperatorFilter line
        Assertions.assertThrows(ClassCastException.class, () -> context.parse(Collections.singletonList(geo)));
    }
}