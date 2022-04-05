package com.navercorp.pinpoint.metric.collector.model.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.metric.collector.model.TelegrafMetric;

import com.navercorp.pinpoint.metric.collector.model.TelegrafMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class TelegrafJsonDeserializerTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectMapper mapper = new ObjectMapper();


    @Test
    public void deserialize_batch() throws IOException {

        InputStream stream = this.getClass().getResourceAsStream("/telegraf-json/telegraf-batch.json");

        TelegrafMetrics systemMetrics = mapper.readValue(stream, TelegrafMetrics.class);
        List<TelegrafMetric> metrics = systemMetrics.getMetrics();

        Assert.assertEquals(2, metrics.size());
        logger.debug("{}", metrics);

        Assert.assertTrue(metrics.get(0).getFields().contains(new TelegrafMetric.Field("field_1", 30)));
        Assert.assertTrue(metrics.get(1).getFields().contains(new TelegrafMetric.Field("field_N", 59)));
    }

    @Test
    public void deserialize_standard() throws IOException {

        InputStream stream = this.getClass().getResourceAsStream("/telegraf-json/telegraf-standard.json");

        TelegrafMetrics systemMetrics = mapper.readValue(stream, TelegrafMetrics.class);
        List<TelegrafMetric> metrics = systemMetrics.getMetrics();

        Assert.assertEquals(1, metrics.size());
        logger.debug("{}", metrics);


        Assert.assertTrue(metrics.get(0).getFields().contains(new TelegrafMetric.Field("field_1", 30)));
    }
}