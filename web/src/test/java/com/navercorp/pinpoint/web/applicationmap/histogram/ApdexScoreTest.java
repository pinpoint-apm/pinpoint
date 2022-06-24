package com.navercorp.pinpoint.web.applicationmap.histogram;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;

public class ApdexScoreTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void getApdexScore() {
        ApdexScore apdexScore = new ApdexScore(100, 0, 100);
        Assertions.assertEquals(1.00, apdexScore.getApdexScore(), 0.001);

        ApdexScore apdexScore1 = new ApdexScore(100, 0, 200);
        Assertions.assertEquals(0.50, apdexScore1.getApdexScore(), 0.001);

        ApdexScore apdexScore2 = new ApdexScore(60, 30, 100);
        Assertions.assertEquals(0.75, apdexScore2.getApdexScore(), 0.001);
    }

    @Test
    public void getApdexScore_floatingPoint() {
        ApdexScore apdexScore = new ApdexScore(Long.MAX_VALUE - 1, 0, Long.MAX_VALUE);
        Assertions.assertEquals(0.999, apdexScore.getApdexScore(), 0.001);
    }

    @Test
    public void getApdexScore_divide_by_zero() {
        ApdexScore apdexScore = new ApdexScore(0, 0, 0);
        Assertions.assertEquals(0, apdexScore.getApdexScore(), 0.001);
    }

    @Test
    public void getApdexScore_format() throws IOException, JSONException {

        final String actualStr = MAPPER.writeValueAsString(new ApdexScore(100, 0, 100));

        JSONAssert.assertEquals("{\"apdexScore\":1.0}", actualStr, JSONCompareMode.LENIENT);
    }

}
