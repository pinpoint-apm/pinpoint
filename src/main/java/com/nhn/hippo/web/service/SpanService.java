package com.nhn.hippo.web.service;

import java.util.List;

import com.nhn.hippo.web.calltree.span.SpanAlign;
import com.nhn.hippo.web.vo.RequestMetadataQuery;
import com.profiler.common.bo.SpanBo;

/**
 *
 */
public interface SpanService {
	List<SpanAlign> selectSpan(String uuid);

	List<SpanBo> selectRequestMetadata(RequestMetadataQuery query);
}
