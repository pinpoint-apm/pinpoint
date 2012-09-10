package com.profiler.context;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class HippoBinaryAnnotation {

	private final long time;
	private final String key;
	private final Object value;
	private final String threadname; // TODO: remove, just for debug.

	public HippoBinaryAnnotation(long time, String key, Object value) {
		this.time = time;
		this.key = key;
		this.value = value;
		this.threadname = Thread.currentThread().getName();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("@={");
		sb.append("time=").append(time);
		sb.append(", key=").append(key);
		sb.append(", value=").append(value);
		sb.append(", threadname=").append(threadname);
		sb.append("}");

		return sb.toString();
	}

	public com.profiler.common.dto.thrift.BinaryAnnotation toThrift() {
		com.profiler.common.dto.thrift.BinaryAnnotation ann = new com.profiler.common.dto.thrift.BinaryAnnotation();

		ann.setTimestamp(time);
		ann.setKey(key);

		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(value);
			ann.setValue(bos.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		}

		ann.setValueType(value.getClass().getName());

		return ann;
	}
}
