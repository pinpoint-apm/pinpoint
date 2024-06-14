package com.navercorp.pinpoint.collector.util;

import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Put;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DurabilityApplierTest {

    @Test
    void null_to_default() {
        DurabilityApplier config = new DurabilityApplier(null);
        assertEquals(Durability.USE_DEFAULT, config.getDurability());
    }

    @Test
    void skipwal() {
        DurabilityApplier config = new DurabilityApplier("SKIP_WAL");
        assertEquals(Durability.SKIP_WAL, config.getDurability());
    }

    @Test
    void skipwal_uppercase() {
        DurabilityApplier configuration = new DurabilityApplier("skip_wal");
        assertEquals(Durability.SKIP_WAL, configuration.getDurability());
    }

    @Test
    void skipwal_unknown() {
        DurabilityApplier configuration = new DurabilityApplier("unknown");
        assertEquals(Durability.USE_DEFAULT, configuration.getDurability());
    }


    @Test
    void testPut() {
        Put put = new Put(new byte[1]);

        DurabilityApplier applier = new DurabilityApplier(Durability.SKIP_WAL.toString());
        applier.apply(put);

        assertEquals(Durability.SKIP_WAL, put.getDurability());
    }
}