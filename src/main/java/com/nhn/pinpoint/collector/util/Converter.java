package com.nhn.pinpoint.collector.util;

import java.nio.ByteBuffer;
import java.util.Date;

import com.nhn.pinpoint.collector.config.TomcatProfilerReceiverConstant;

public class Converter {
	public static ByteBuffer toByteBuffer(String data) {
		return ByteBuffer.wrap(data.getBytes());
	}

	public static String toString(ByteBuffer data) {
		return new String(data.array());
	}

	public static String toHmsMs(byte[] byteLong) {
		return toHmsMs(Long.parseLong(new String(byteLong)));

	}

	public static String toHmsMs(long dateTime) {
		return TomcatProfilerReceiverConstant.DATE_FORMAT_HMS_MS.format(new Date(dateTime));
	}
}
