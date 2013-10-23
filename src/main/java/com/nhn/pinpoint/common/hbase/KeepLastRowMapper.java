package com.nhn.pinpoint.common.hbase;

import org.apache.hadoop.hbase.client.Result;
import org.springframework.data.hadoop.hbase.RowMapper;

import com.nhn.pinpoint.common.hbase.KeepLastRowValue;

/**
 * 
 * @author netspider
 * 
 * @param <T>
 */
@Deprecated
public interface KeepLastRowMapper<T> extends RowMapper<T> {
	public KeepLastRowValue<T> mapRowAndReturnLastRow(Result result, int rowNum) throws Exception;
}
