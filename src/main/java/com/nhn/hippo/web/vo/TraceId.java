package com.nhn.hippo.web.vo;

import java.util.Arrays;

public class TraceId {

	private final byte[] id;
	private final int hashcode;

	public TraceId(byte[] traceId) {
		this.id = traceId;
		this.hashcode = new String(id).hashCode();
	}

	public byte[] getBytes() {
		return id;
	}

	@Override
	public boolean equals(Object traceId) {
		if (traceId instanceof TraceId) {
			return Arrays.equals(id, ((TraceId) traceId).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	public String toString() {
		return Arrays.toString(id);
	}
}
