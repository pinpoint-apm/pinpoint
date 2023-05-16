package com.navercorp.pinpoint.common.hbase.rowmapper;

import com.navercorp.pinpoint.common.hbase.RowMapper;
import org.apache.hadoop.hbase.client.Result;

public class ResultSizeMapper<T> implements RowMapper<T> {
    private int resultSize;
    @Override
    public T mapRow(Result result, int rowNum) throws Exception {
        this.resultSize += result.size();
        return null;
    }

    public int getResultSize() {
        return resultSize;
    }
}
