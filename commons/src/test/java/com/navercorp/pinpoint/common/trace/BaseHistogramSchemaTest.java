package com.navercorp.pinpoint.common.trace;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class BaseHistogramSchemaTest {

    @Test
    void findHistogramSlot() {
        HistogramSchema schema = BaseHistogramSchema.NORMAL_SCHEMA;

        HistogramSlot fastSuccess = schema.findHistogramSlot(1000, false);
        Assertions.assertEquals(schema.getFastSlot(), fastSuccess);

        HistogramSlot fastFailed = schema.findHistogramSlot(1000, true);
        Assertions.assertEquals(schema.getFastErrorSlot(), fastFailed);
    }
}