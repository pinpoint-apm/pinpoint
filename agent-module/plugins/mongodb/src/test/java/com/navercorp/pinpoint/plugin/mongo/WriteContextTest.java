package com.navercorp.pinpoint.plugin.mongo;

import com.mongodb.client.model.Filters;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.bson.BsonBinary;
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