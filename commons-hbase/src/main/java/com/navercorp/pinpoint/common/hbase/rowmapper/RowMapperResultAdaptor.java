package com.navercorp.pinpoint.common.hbase.rowmapper;

import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.util.Assert;
import org.apache.hadoop.hbase.client.Result;


public class RowMapperResultAdaptor<T> implements RowMapper<T> {
    private final ResultHandler resultHandler;
    private final RowMapper<T> delegate;

    public RowMapperResultAdaptor(RowMapper<T> delegate, ResultHandler resultConsumer) {
        this.delegate = Assert.requireNonNull(delegate, "delegate");
        this.resultHandler = Assert.requireNonNull(resultConsumer, "resultConsumer");
    }


    @Override
    public T mapRow(Result result, int rowNum) throws Exception {
        this.resultHandler.mapRow(result, rowNum);
        return delegate.mapRow(result, rowNum);
    }

}
