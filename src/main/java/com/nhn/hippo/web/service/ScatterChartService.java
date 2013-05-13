package com.nhn.hippo.web.service;

import java.util.List;

import com.nhn.hippo.web.filter.Filter;
import com.nhn.hippo.web.vo.TransactionMetadataQuery;
import com.nhn.hippo.web.vo.scatter.Dot;
import com.profiler.common.bo.SpanBo;

public interface ScatterChartService {
	public List<Dot> selectScatterData(String applicationName, long from, long to, int limit, Filter filter);
	
	public List<SpanBo> selectTransactionMetadata(TransactionMetadataQuery query);
}
