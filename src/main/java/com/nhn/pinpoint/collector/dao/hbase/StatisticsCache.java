package com.nhn.pinpoint.collector.dao.hbase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.primitives.Bytes;

/**
 * 
 * @author netspider
 * 
 */
@NotThreadSafe
public class StatisticsCache {

	final long threshold = 10L;

	final Map<Key, Value> c1 = new HashMap<Key, Value>(1024);
	final Map<Key, Value> c2 = new HashMap<Key, Value>(1024);

	boolean master = true;

	private static class Key {
		byte[] v;

		public Key(byte[] v) {
			this.v = v;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(v);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (!Arrays.equals(v, other.v))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Key [v=" + Arrays.toString(v) + "]";
		}
	}

	@NotThreadSafe
	public static class Value {
		byte[] rowKey;
		byte[] columnName;
		long value;

		public Value(byte[] rowKey, byte[] columnName, long value) {
			this.rowKey = rowKey;
			this.columnName = columnName;
			this.value = value;
		}

		public byte[] getRowKey() {
			return rowKey;
		}

		public byte[] getColumnName() {
			return columnName;
		}

		public long getValue() {
			return value;
		}

		public void addValue(long value) {
			this.value += value;
		}
	}

	private Map<Key, Value> getMaster() {
		return (master) ? c1 : c2;
	}

	private Map<Key, Value> getSlave() {
		return (master) ? c2 : c1;
	}

	public void add(byte[] rowKey, byte[] columnName, long value) {
		Key key = new Key(Bytes.concat(rowKey, columnName));
		Map<Key, Value> c = getMaster();
		if (c.containsKey(key)) {
			c.get(key).addValue(value);
		} else {
			c.put(key, new Value(rowKey, columnName, value));
		}
	}

	public List<Value> getItems() {
		getSlave().clear();
		master ^= true;

		List<Value> list = new ArrayList<Value>();

		Set<Entry<Key, Value>> entrySet = getSlave().entrySet();
		for (Entry<Key, Value> entry : entrySet) {
			Value itm = entry.getValue();
			if (threshold < itm.getValue()) {
				list.add(itm);
			} else {
				add(itm.getRowKey(), itm.getColumnName(), itm.getValue());
			}
		}

		getSlave().clear();
		return list;
	}

	public List<Value> getAllItems() {
		getSlave().clear();
		master ^= true;

		List<Value> list = new ArrayList<Value>();

		Set<Entry<Key, Value>> entrySet = getSlave().entrySet();
		for (Entry<Key, Value> entry : entrySet) {
			Value itm = entry.getValue();
			list.add(itm);
		}

		getSlave().clear();
		return list;
	}
}
