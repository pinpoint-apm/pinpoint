package com.navercorp.pinpoint.web.applicationmap.histogram;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class ApdexScoreTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void getApdexScore() {
        Assert.assertEquals(1.00, new ApdexScore(100, 0, 100).getApdexScore(), 0.001);
        Assert.assertEquals(0.50, new ApdexScore(100, 0, 200).getApdexScore(), 0.001);
    }

    @Test
    public void getApdexScore_floatingPoint() {
        Assert.assertEquals(0.999, new ApdexScore(Long.MAX_VALUE - 1, 0, Long.MAX_VALUE).getApdexScore(), 0.001);
    }

    @Test
    public void getApdexScore_format() throws IOException, JSONException {
        ObjectMapper objectMapper = new ObjectMapper();
        final String actualStr = objectMapper.writeValueAsString(new ApdexScore(100, 0, 100));
        JSONAssert.assertEquals("{\"apdexScore\":1.0}", actualStr, JSONCompareMode.LENIENT);
    }

}
