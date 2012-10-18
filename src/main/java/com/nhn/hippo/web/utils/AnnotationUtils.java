package com.nhn.hippo.web.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.profiler.common.util.AnnotationTranscoder;

public class AnnotationUtils {

	private static final AnnotationTranscoder transcoder = new AnnotationTranscoder();
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");

	public static String bytesToString(int dataType, byte[] data) {
		Object decoded = transcoder.decode(dataType, data);
		if (decoded != null)
			return decoded.toString();
		else
			return null;
	}

	public static String longToDateStr(long date) {
		return dateFormat.format(new Date(date));
	}

	public static String longLongToUUID(long mostTraceId, long leastTraceId) {
		return new UUID(mostTraceId, leastTraceId).toString();
	}
}
