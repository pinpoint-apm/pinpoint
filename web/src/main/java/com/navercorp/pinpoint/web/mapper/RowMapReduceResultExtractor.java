package com.navercorp.pinpoint.web.mapper;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.RowReducer;

import java.util.Objects;

public class RowMapReduceResultExtractor<T> implements ResultsExtractor<T>{
    private final RowMapper<T> rowMapper;
    private final RowReducer<T> rowReducer;
    
    public RowMapReduceResultExtractor(RowMapper<T> rowMapper, RowReducer<T> rowReducer) {
        this.rowMapper = Objects.requireNonNull(rowMapper, "rowMapper");
        this.rowReducer = rowReducer;
    }
    
    @Override
    public T extractData(ResultScanner results) throws Exception {
        int rowNum = 0;
        T r = null;
        for (Result result : results) {
            T map = this.rowMapper.mapRow(result, rowNum++);
            r = rowReducer.reduce(map);
        }
        
        return r;
    }
}