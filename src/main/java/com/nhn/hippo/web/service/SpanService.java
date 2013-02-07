package com.nhn.hippo.web.service;

import com.nhn.hippo.web.calltree.span.SpanAlign;
import com.nhn.hippo.web.vo.RequestMetadata;
import com.nhn.hippo.web.vo.RequestMetadataQuery;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface SpanService {
	List<SpanAlign> selectSpan(String uuid);

	Map<String, RequestMetadata> selectRequestMetadata(RequestMetadataQuery query);
}
