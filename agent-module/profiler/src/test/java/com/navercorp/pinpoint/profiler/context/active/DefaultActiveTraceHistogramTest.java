package com.navercorp.pinpoint.profiler.context.active;

import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultActiveTraceHistogramTest {

    @Test
    void getCounter() {
        HistogramSchema schema = BaseHistogramSchema.NORMAL_SCHEMA;
        DefaultActiveTraceHistogram histogram = new DefaultActiveTraceHistogram(schema);

        for (int i = 0; i < 1; i++) {
            histogram.increment(schema.getFastSlot());
        }

        for (int i = 0; i < 2; i++) {
            histogram.increment(schema.getNormalSlot());
        }

        for (int i = 0; i < 3; i++) {
            histogram.increment(schema.getSlowSlot());
        }

        for (int i = 0; i < 4; i++) {
            histogram.increment(schema.getVerySlowSlot());
        }

        List<Integer> counter = histogram.getCounter();
        assertThat(counter).hasSize(4)
                .containsExactly(1, 2, 3, 4);
    }
}