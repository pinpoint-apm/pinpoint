package com.navercorp.pinpoint.common.hbase.rowmapper;

import com.navercorp.pinpoint.common.hbase.RowMapper;
import org.apache.hadoop.hbase.client.Result;

import java.util.Objects;


public class RowMapperResultAdaptor<T> implements RowMapper<T> {
    private final RowMapper<T> consumer;
    private final RowMapper<T> delegate;

    public RowMapperResultAdaptor(RowMapper<T> delegate, RowMapper<T> consumer) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.consumer = Objects.requireNonNull(consumer, "consumer");
    }


    @Override
    public T mapRow(Result result, int rowNum) throws Exception {
        this.consumer.mapRow(result, rowNum);
        return delegate.mapRow(result, rowNum);
    }

}
