package com.nhn.pinpoint.common.hbase;

import org.apache.hadoop.hbase.KeyValue;

/**
 * 
 * @author netspider
 * 
 */
@Deprecated
public interface LastRowHandler {
	public void handle(final KeyValue keyValue);
}
