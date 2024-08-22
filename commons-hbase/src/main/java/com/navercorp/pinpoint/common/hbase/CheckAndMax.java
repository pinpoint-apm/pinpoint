package com.navercorp.pinpoint.common.hbase;


import com.navercorp.pinpoint.common.hbase.util.CheckAndMutates;
import org.apache.hadoop.hbase.client.CheckAndMutate;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.Objects;

public final class CheckAndMax {
    private final byte[] row;
    private final byte[] family;
    private final byte[] qualifier;
    private final long value;

    public CheckAndMax(byte[] row, byte[] family, byte[] qualifier, long value) {
        this.row = Objects.requireNonNull(row, "row");
        this.family = Objects.requireNonNull(family, "family");
        this.qualifier = Objects.requireNonNull(qualifier, "qualifier");
        this.value = value;
    }

    public byte[] row() {
        return row;
    }

    public byte[] family() {
        return family;
    }

    public byte[] qualifier() {
        return qualifier;
    }

    public long value() {
        return value;
    }

    public static CheckAndMutate initialMax(CheckAndMax max) {
        Put put = new Put(max.row(), true);
        put.addColumn(max.family(), max.qualifier(), Bytes.toBytes(max.value()));

        return CheckAndMutate.newBuilder(max.row())
                .ifNotExists(max.family(), max.qualifier())
                .build(put);
    }

    public static CheckAndMutate casMax(CheckAndMutate mutate) {
        Objects.requireNonNull(mutate, "mutate");
        return CheckAndMutates.max(mutate.getRow(), mutate.getFamily(), mutate.getQualifier(), mutate.getValue(), (Put) mutate.getAction());
    }

}
