package com.nhn.pinpoint.web.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhn.pinpoint.common.HistogramSchema;
import junit.framework.Assert;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.applicationmap.histogram.Histogram;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * @author emeroad
 */
public class HistogramSerializerTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void testSerialize() throws Exception {
        Histogram original = new Histogram(ServiceType.TOMCAT);
        HistogramSchema schema = original.getHistogramSchema();
        original.addCallCount(schema.getFastSlot().getSlotTime(), 1);
        original.addCallCount(schema.getNormalSlot().getSlotTime(), 2);
        original.addCallCount(schema.getSlowSlot().getSlotTime(), 3);
        original.addCallCount(schema.getVerySlowSlot().getSlotTime(), 4);
        original.addCallCount(schema.getErrorSlot().getSlotTime(), 5);

        String jacksonJson = objectMapper.writeValueAsString(original);
        HashMap objectMapperHashMap = objectMapper.readValue(jacksonJson, HashMap.class);

        logger.debug(jacksonJson);

        String internalJson = internalJson(original);
        HashMap hashMap = objectMapper.readValue(internalJson, HashMap.class);

        Assert.assertEquals(objectMapperHashMap, hashMap);
    }

    /**
     * 과거 버전의 histogam에 손으로 작성한 json코드를 테스트를 위해 testcase로 이동시킴.
     * @param histogram
     * @return
     */
    public String internalJson(Histogram histogram) {
        HistogramSchema histogramSchema = histogram.getHistogramSchema();
        final StringBuilder sb = new StringBuilder(128);
        sb.append("{ ");

        appendSlotTimeAndCount(sb, histogramSchema.getFastSlot().getSlotName(), histogram.getFastCount());
        sb.append(", ");
        appendSlotTimeAndCount(sb, histogramSchema.getNormalSlot().getSlotName(), histogram.getNormalCount());
        sb.append(", ");
        appendSlotTimeAndCount(sb, histogramSchema.getSlowSlot().getSlotName(), histogram.getSlowCount());
        sb.append(", ");
        // very slow는 0값이라 slow 값을 사용해야 한다.
        appendSlotTimeAndCount(sb, histogramSchema.getVerySlowSlot().getSlotName(), histogram.getVerySlowCount());
        sb.append(", ");
        appendSlotTimeAndCount(sb, histogramSchema.getErrorSlot().getSlotName(), histogram.getErrorCount());
        sb.append(" }");

        return sb.toString();
    }

    private void appendSlotTimeAndCount(StringBuilder sb, String slotTimeName, long count) {
        sb.append('"');
        sb.append(slotTimeName);
        sb.append('"');
        sb.append(":");
        sb.append(count);
    }
}
