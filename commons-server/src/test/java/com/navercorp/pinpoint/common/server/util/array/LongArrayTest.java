package com.navercorp.pinpoint.common.server.util.array;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LongArrayTest {

    @Test
    void asLongList() {
        LongRecord longRecord1 = new LongRecord(1L);
        LongRecord longRecord2 = new LongRecord(2L);
        LongRecord longRecord3 = new LongRecord(3L);

        List<LongRecord> records = List.of(longRecord1, longRecord2, longRecord3);
        List<Long> longList = LongArray.asList(records, LongRecord::value);
        assertThat(longList).containsExactly(1L, 2L, 3L);
    }

    record LongRecord(long value) {
    }

    @Test
    void asLongList_linkedList() {
        LongRecord longRecord2 = new LongRecord(2L);
        LongRecord longRecord1 = new LongRecord(1L);
        LongRecord longRecord3 = new LongRecord(3L);

        List<LongRecord> records = new LinkedList<>();
        records.add(longRecord1);
        records.add(longRecord2);
        records.add(longRecord3);
        List<Long> longList = LongArray.asList(records, LongRecord::value);
        assertThat(longList).containsExactly(1L, 2L, 3L);
    }
}