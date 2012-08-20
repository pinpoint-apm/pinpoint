package com.profiler.context;

public class BinaryAnnotation extends Annotation {

	protected final byte[] binaryValue;

	/**
	 * @param timestamp
	 *            when was this annotation created? microseconds from epoch
	 * @param value
	 *            detailed description of what happened at the timestamp
	 * @param host
	 *            host this annotation was created on
	 */
	public BinaryAnnotation(long timestamp, byte[] value, EndPoint host) {
		super(timestamp, null, host);
		this.binaryValue = value;
	}
}
