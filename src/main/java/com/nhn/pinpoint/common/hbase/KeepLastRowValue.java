package com.nhn.pinpoint.common.hbase;

import org.apache.hadoop.hbase.KeyValue;

public class KeepLastRowValue<T> {
	private final KeyValue lastRow;
	private final T value;
	
	public KeepLastRowValue(T value, KeyValue lastRow) {
		this.value = value;
		this.lastRow = lastRow;
	}

	public KeyValue getLastRow() {
		return lastRow;
	}

	public T getValue() {
		return value;
	}
}
