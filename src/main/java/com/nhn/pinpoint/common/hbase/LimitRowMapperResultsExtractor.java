package com.nhn.pinpoint.common.hbase;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.springframework.data.hadoop.hbase.ResultsExtractor;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.util.Assert;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class LimitRowMapperResultsExtractor<T> implements ResultsExtractor<List<T>> {

    private int limit = Integer.MAX_VALUE;
    private final RowMapper<T> rowMapper;
    private KeyValue lastestMappedKeyValue;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    public KeyValue getLastestMappedKeyValue() {
		return lastestMappedKeyValue;
	}

	/**
     * Create a new RowMapperResultSetExtractor.
     *
     * @param rowMapper the RowMapper which creates an object for each row
     */
    public LimitRowMapperResultsExtractor(RowMapper<T> rowMapper, int limit) {
        Assert.notNull(rowMapper, "RowMapper is required");
        this.rowMapper = rowMapper;
        this.limit = limit;
    }

	public List<T> extractData(ResultScanner results) throws Exception {
		List<T> rs = new ArrayList<T>();
		int rowNum = 0;
		
		KeepLastRowMapper<T> keepLastRowMapper = (this.rowMapper instanceof KeepLastRowMapper) ? (KeepLastRowMapper<T>) rowMapper : null;
		
		for (Result result : results) {
			T t;
			if (keepLastRowMapper == null) {
				t = this.rowMapper.mapRow(result, rowNum++);
			} else {
				KeepLastRowValue<T> v = keepLastRowMapper.mapRowAndReturnLastRow(result, rowNum++);
				t = v.getValue();
				lastestMappedKeyValue = v.getLastRow();
			}
			if (t instanceof Collection) {
				rowNum += ((Collection<?>) t).size();
			} else if (t instanceof Map) {
				rowNum += ((Map<?, ?>) t).size();
			} else if (t == null) {
				// empty
			} else if (t.getClass().isArray()) {
				rowNum += Array.getLength(t);
			} else {
				rowNum++;
			}
			rs.add(t);
			if (rowNum >= limit) {
				break;
			}
		}
		return rs;
	}
}
