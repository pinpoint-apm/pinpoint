package com.navercorp.pinpoint.common.server.util.array;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DoubleArrayTest {
    @Test
    void asDoubleList() {
        DoubleRecord doubleRecord1 = new DoubleRecord(1.0);
        DoubleRecord doubleRecord2 = new DoubleRecord(2.0);
        DoubleRecord doubleRecord3 = new DoubleRecord(3.0);

        List<DoubleRecord> records = List.of(doubleRecord1, doubleRecord2, doubleRecord3);
        List<Double> doubleList = DoubleArray.asList(records, DoubleRecord::value);
        assertThat(doubleList).containsExactly(1.0, 2.0, 3.0);
    }

    record DoubleRecord(double value) {
    }

}