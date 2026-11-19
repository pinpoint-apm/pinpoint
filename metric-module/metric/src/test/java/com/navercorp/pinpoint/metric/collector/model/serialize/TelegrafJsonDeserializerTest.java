package com.navercorp.pinpoint.metric.collector.model.serialize;

import tools.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.metric.collector.model.TelegrafMetric;
import com.navercorp.pinpoint.metric.collector.model.TelegrafMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TelegrafJsonDeserializerTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectMapper mapper = Jackson.newMapper();


    @Test
    public void deserialize_batch() throws IOException {

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
    public void deserialize_standard() throws IOException {

        InputStream stream = this.getClass().getResourceAsStream("/telegraf-json/telegraf-standard.json");

        TelegrafMetrics systemMetrics = mapper.readValue(stream, TelegrafMetrics.class);
        List<TelegrafMetric> metrics = systemMetrics.getMetrics();

        assertThat(metrics)
                .hasSize(1)
                .flatMap(TelegrafMetric::getFields)
                .containsOnlyOnce(new TelegrafMetric.Field("field_1", 30));
    }

    @Test
    public void deserialize_batch_failOnTrailingTokens() {
        InputStream stream = this.getClass().getResourceAsStream("/telegraf-json/telegraf-batch.json");

        TelegrafMetrics systemMetrics = strictMapper().readValue(stream, TelegrafMetrics.class);

        assertThat(systemMetrics.getMetrics()).hasSize(2);
    }

    @Test
    public void deserialize_batch_skipFieldAfterMetrics() {
        String json = """
                {"metrics":[{"fields":{"field_1":30},"name":"cpu","tags":{"host":"host1"},"timestamp":1700000000}],"extra":{"a":[1,2]}}
                """;

        TelegrafMetrics systemMetrics = strictMapper().readValue(json, TelegrafMetrics.class);

        assertThat(systemMetrics.getMetrics()).hasSize(1);
    }

    private static JsonMapper strictMapper() {
        return Jackson.newMapper().rebuild()
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .build();
    }
}