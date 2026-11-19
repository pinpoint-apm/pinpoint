package com.navercorp.pinpoint.metric.collector.model.serialize;

import tools.jackson.databind.json.JsonMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.metric.collector.model.TelegrafMetric;
import com.navercorp.pinpoint.metric.collector.model.TelegrafMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TelegrafJsonDeserializerTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final JsonMapper mapper = Jackson.newMapper();


    @Test
    public void deserialize_batch() {

        InputStream stream = this.getClass().getResourceAsStream("/telegraf-json/telegraf-batch.json");

        TelegrafMetrics systemMetrics = mapper.readValue(stream, TelegrafMetrics.class);
        List<TelegrafMetric> metrics = systemMetrics.getMetrics();

        assertThat(metrics)
                .hasSize(2)
                .flatMap(TelegrafMetric::getFields)
                .contains(new TelegrafMetric.Field("field_1", 30),
                        new TelegrafMetric.Field("field_N", 59));
    }

    @Test
    public void deserialize_standard() {

        InputStream stream = this.getClass().getResourceAsStream("/telegraf-json/telegraf-standard.json");

        TelegrafMetrics systemMetrics = mapper.readValue(stream, TelegrafMetrics.class);
        List<TelegrafMetric> metrics = systemMetrics.getMetrics();

        assertThat(metrics)
                .hasSize(1)
                .flatMap(TelegrafMetric::getFields)
                .containsOnlyOnce(new TelegrafMetric.Field("field_1", 30));
    }
}