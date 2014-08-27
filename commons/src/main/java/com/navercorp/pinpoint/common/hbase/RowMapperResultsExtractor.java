package com.nhn.pinpoint.common.hbase;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring꺼가 package라서 그냥 복사해옴.
 * copy->Spring
 * Adapter encapsulating the RowMapper callback.
 *
 * @author Costin Leau
 */
class RowMapperResultsExtractor<T> implements ResultsExtractor<List<T>> {

    private final RowMapper<T> rowMapper;

    /**
     * Create a new RowMapperResultSetExtractor.
     *
     * @param rowMapper the RowMapper which creates an object for each row
     */
    public RowMapperResultsExtractor(RowMapper<T> rowMapper) {
        Assert.notNull(rowMapper, "RowMapper is required");
        this.rowMapper = rowMapper;
    }

    public List<T> extractData(ResultScanner results) throws Exception {
        List<T> rs = new ArrayList<T>();
        int rowNum = 0;
        for (Result result : results) {
            rs.add(this.rowMapper.mapRow(result, rowNum++));
        }
        return rs;
    }
}
