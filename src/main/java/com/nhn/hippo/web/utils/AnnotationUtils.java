package com.nhn.hippo.web.utils;

import com.profiler.common.util.AnnotationTranscoder;

public class AnnotationUtils {

	private static final AnnotationTranscoder transcoder = new AnnotationTranscoder();

	public static String bytesToString(int dataType, byte[] data) {
		Object decoded = transcoder.decode(dataType, data);
		if (decoded != null)
			return decoded.toString();
		else
			return null;
	}

}
