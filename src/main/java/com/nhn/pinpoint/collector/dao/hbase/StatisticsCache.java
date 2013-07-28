package com.nhn.pinpoint.collector.dao.hbase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.primitives.Bytes;

/**
 * 
 * @author netspider
 * 
 */
public class StatisticsCache {

	final long threshold = 10L;
	final ConcurrentMap<Key, Value> cache = new ConcurrentHashMap<Key, Value>(1024);

	public long add(byte[] rowKey, byte[] columnName, long value) {
		Key key = new Key(Bytes.concat(rowKey, columnName));
		Value v = cache.putIfAbsent(key, new Value(rowKey, columnName, value));
		if (v != null) {
			return v.addValue(value);
		} else {
			return value;
		}
	}

	public List<Value> getItems() {
		List<Value> result = new ArrayList<Value>();

		Set<Entry<Key, Value>> entrySet = cache.entrySet();

		for (Entry<Key, Value> entry : entrySet) {
			Key key = entry.getKey();
			Value value = entry.getValue();

			if (value.getLongValue() < threshold) {
				continue;
			}

			result.add(cache.remove(key));
		}

		return result;
	}

	public List<Value> getAllItems() {
		List<Value> result = new ArrayList<Value>();
		Set<Entry<Key, Value>> entrySet = cache.entrySet();
		for (Entry<Key, Value> entry : entrySet) {
			result.add(cache.remove(entry.getKey()));
		}
		return result;
	}

	public int size() {
		return cache.size();
	}

	public static class Key {
		final byte[] v;

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

	public static class Value {
		final byte[] rowKey;
		final byte[] columnName;
		final AtomicLong value;

		public Value(byte[] rowKey, byte[] columnName, long value) {
			this.rowKey = rowKey;
			this.columnName = columnName;
			this.value = new AtomicLong(value);
		}

		public byte[] getRowKey() {
			return rowKey;
		}

		public byte[] getColumnName() {
			return columnName;
		}

		public AtomicLong getValue() {
			return value;
		}

		public long popLongValue() {
			return value.getAndSet(0L);
		}

		public long getLongValue() {
			return value.get();
		}

		public long addValue(long value) {
			return this.value.addAndGet(value);
		}
	}
}
