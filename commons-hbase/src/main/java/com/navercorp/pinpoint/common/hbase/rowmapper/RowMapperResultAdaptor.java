package com.navercorp.pinpoint.common.hbase.rowmapper;

import com.navercorp.pinpoint.common.hbase.RowMapper;
import org.apache.hadoop.hbase.client.Result;

import java.util.Objects;


public class RowMapperResultAdaptor<T> implements RowMapper<T> {
    private final ResultHandler resultHandler;
    private final RowMapper<T> delegate;

    public RowMapperResultAdaptor(RowMapper<T> delegate, ResultHandler resultConsumer) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.resultHandler = Objects.requireNonNull(resultConsumer, "resultConsumer");
    }


    @Override
    public T mapRow(Result result, int rowNum) throws Exception {
        this.resultHandler.mapRow(result, rowNum);
        return delegate.mapRow(result, rowNum);
    }

}
