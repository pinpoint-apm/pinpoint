package com.nhn.pinpoint.collector.dao.hbase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.hadoop.hbase.client.Increment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Bytes;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author netspider
 * 
 */
public class StatisticsCache {

	public static interface FlushHandler {
		public void handleValue(Value value);

		public void handleValue(Increment increment);
	}

	private final int bufferSize = 512;
	private final int cacheSize;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final FlushHandler flushHandler;
	private final ConcurrentMap<Key, Value> cache;

	// TODO buffer를 threadlocal에 보관하지 않고 thread id 별 map에 보관해도 문제 없을 듯.
	private final ThreadLocal<Map<Key, Value>> bufferMap = new ThreadLocal<Map<Key, Value>>();
	private final List<Map<Key, Value>> bufferList = new ArrayList<Map<Key, Value>>();

	private final Lock bufferListLock = new ReentrantLock();
	private final Lock cacheLock = new ReentrantLock();

	public StatisticsCache(FlushHandler flushHandler) {
		this(5000, flushHandler);
	}

	public StatisticsCache(int cacheSize, FlushHandler flushHandler) {
		this.cacheSize = cacheSize;
		this.flushHandler = flushHandler;
		this.cache = new ConcurrentHashMap<Key, Value>(cacheSize);
	}

	private Map<Key, Value> getLocalBuffer() {
		Map<Key, Value> buffer = bufferMap.get();
		if (buffer == null) {
			buffer = new HashMap<StatisticsCache.Key, StatisticsCache.Value>(bufferSize);
			bufferList.add(buffer);
			bufferMap.set(buffer);
		}
		return buffer;
	}

	private void addValues(Map<Key, Value> valueMap) {
		try {
			cacheLock.lock();
			for (Entry<Key, Value> entry : valueMap.entrySet()) {
				Key key = entry.getKey();
				Value value = cache.get(key);
				if (value == null) {
					value = entry.getValue();
				} else {
					value.addValue(entry.getValue().getLongValue());
				}
				cache.put(key, value);
			}
		} finally {
			cacheLock.unlock();
		}
	}

	private void drainLocalBuffer() {
		Map<Key, Value> buffer = bufferMap.get();
		if (buffer != null) {
			addValues(buffer);
			buffer.clear();
		}
	}

	private void drainAllLocalBuffer() {
		try {
			bufferListLock.lock();
			for (Map<Key, Value> buffer : bufferList) {
				if (buffer == null) {
					continue;
				}
				addValues(buffer);
				buffer.clear();
			}
		} finally {
			bufferListLock.unlock();
		}
	}

	public void add(byte[] rowKey, byte[] columnName, long value) {
		try {
			Key key = new Key(Bytes.concat(rowKey, columnName));

			if (add(key, rowKey, columnName, value)) {
				return;
			}
			flush();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private boolean add(Key key, byte[] rowKey, byte[] columnName, long value) {
		Map<Key, Value> buffer = getLocalBuffer();

		Value bufferValue = buffer.get(key);

		if (bufferValue == null) {
			bufferValue = new Value(rowKey, columnName, value);
			buffer.put(key, bufferValue);
		} else {
			bufferValue.addValue(value);
		}

		return (buffer.size() < bufferSize);
	}

	private void flushCache(int checkSize) {
		try {
			cacheLock.lock();

			if (cache.size() <= checkSize) {
				return;
			}

			Set<Entry<Key, Value>> entrySet = cache.entrySet();
			for (Entry<Key, Value> entry : entrySet) {
				Value v = cache.remove(entry.getKey());
				if (v != null) {
					flushHandler.handleValue(v);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			cacheLock.unlock();
		}
	}

	public void flush() {
		drainLocalBuffer();
		flushCache(cacheSize);
	}

	public void flushAll() {
		drainAllLocalBuffer();
		flushCache(0);
	}

	public int size() {
		return cache.size();
	}

	public boolean isEmpty() {
		return cache.isEmpty();
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
