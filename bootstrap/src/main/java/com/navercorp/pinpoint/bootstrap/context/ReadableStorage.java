package com.nhn.pinpoint.bootstrap.context;

import java.util.List;

import com.nhn.pinpoint.common.bo.SpanEventBo;

/**
 * @author Hyun Jeong
 */
public interface ReadableStorage {
	public  List<SpanEventBo> getSpanEventList();
}
