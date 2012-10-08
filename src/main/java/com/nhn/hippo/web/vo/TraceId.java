package com.nhn.hippo.web.vo;

import java.util.Arrays;

public class TraceId {

	private final byte[] id;

	public TraceId(byte[] traceId) {
		this.id = traceId;
	}

	public byte[] getBytes() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(id);
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
		TraceId other = (TraceId) obj;
		if (!Arrays.equals(id, other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TraceId [id=" + Arrays.toString(id) + "]";
	}
}
