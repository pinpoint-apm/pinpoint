package com.profiler.context;

/**
 * 
 * @author netspider
 * 
 */
public class HippoAnnotation {

	private final long timestamp;
	private final Long duration;
	private final String key;
	private final int valueTypeCode;
	private final byte[] value;
	private final String threadname;

	public HippoAnnotation(long timestamp, String key, Long duration) {
		this.timestamp = timestamp;
		this.duration = duration;
		this.key = key;
		this.valueTypeCode = -1;
		this.value = null;
		this.threadname = Thread.currentThread().getName();
	}

	public HippoAnnotation(long timestamp, String key, int valueTypeCode, byte[] value, Long duration) {
		this.timestamp = timestamp;
		this.duration = duration;
		this.key = key;
		this.valueTypeCode = valueTypeCode;
		this.value = value;
		this.threadname = Thread.currentThread().getName();
	}

	public String getKey() {
		return this.key;
	}

	public long getTimestamp() {
		return this.timestamp;
	}
	
	@Override
	public String toString() {
		return "HippoAnnotation [timestamp=" + timestamp + ", duration=" + duration + ", key=" + key + ", valueTypeCode=" + valueTypeCode + ", value=" + value + ", threadname=" + threadname + "]";
	}

	public com.profiler.common.dto.thrift.Annotation toThrift() {
		com.profiler.common.dto.thrift.Annotation ann = new com.profiler.common.dto.thrift.Annotation();

		ann.setTimestamp(timestamp);

		if (duration != null) {
			ann.setDuration(duration);
		}

		ann.setKey(key);
		ann.setValueTypeCode(valueTypeCode);
		ann.setValue(value);

		return ann;
	}
}
