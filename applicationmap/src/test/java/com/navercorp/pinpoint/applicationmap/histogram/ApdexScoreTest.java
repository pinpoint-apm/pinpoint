package com.navercorp.pinpoint.applicationmap.histogram;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.util.List;

public class ApdexScoreTest {

    private final ObjectMapper MAPPER = Jackson.newMapper();

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
    public void merge() {
        ApdexScore merged = ApdexScore.newApdexScore(List.of(
                new ApdexScore(60, 30, 100),
                new ApdexScore(40, 10, 100)
        ));

        Assertions.assertEquals(new ApdexScore.ApdexFormula(100, 40, 200), merged.getApdexFormula());
        Assertions.assertEquals(0.60, merged.getApdexScore(), 0.001);
    }

    @Test
    public void merge_empty() {
        ApdexScore merged = ApdexScore.newApdexScore(List.of());

        Assertions.assertEquals(new ApdexScore.ApdexFormula(0, 0, 0), merged.getApdexFormula());
        Assertions.assertEquals(0, merged.getApdexScore(), 0.001);
    }

    @Test
    public void getApdexScore_format() throws IOException, JSONException {

        final String actualStr = MAPPER.writeValueAsString(new ApdexScore(100, 0, 100));

        JSONAssert.assertEquals("{\"apdexScore\":1.0}", actualStr, JSONCompareMode.LENIENT);
    }

    @Test
    public void getApdexScore_formula() throws JsonProcessingException, JSONException {
        ApdexScore apdexScore = new ApdexScore(100, 50, 200);
        final String actualStr = MAPPER.writeValueAsString(apdexScore);

        JSONAssert.assertEquals("{\"apdexScore\":0.625,\"apdexFormula\":{\"satisfiedCount\":100,\"toleratingCount\":50,\"totalSamples\":200}}", actualStr, JSONCompareMode.LENIENT);
    }

}
