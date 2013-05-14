package com.nhn.pinpoint.web.service;

import java.util.List;

import com.nhn.pinpoint.web.filter.Filter;
import com.nhn.pinpoint.web.vo.TraceId;
import com.nhn.pinpoint.web.vo.TransactionMetadataQuery;
import com.nhn.pinpoint.web.vo.scatter.Dot;
import com.profiler.common.bo.SpanBo;

public interface ScatterChartService {

	/**
	 * 필터를 사용한 검색.
	 * 
	 * @param traceIds
	 * @param applicationName
	 * @param filter
	 * @return
	 */
	public List<Dot> selectScatterData(List<TraceId> traceIds, String applicationName, Filter filter);

	/**
	 * 전체 데이터 검색.
	 * 
	 * @param applicationName
	 * @param from
	 * @param to
	 * @param limit
	 * @return
	 */
	public List<Dot> selectScatterData(String applicationName, long from, long to, int limit);

	/**
	 * scatter dot을 limit 개수만큼 잘라서 조회하기 위해서 사용된다.
	 * 
	 * @param applicationName
	 * @param from
	 * @param to
	 * @param limit
	 * @return
	 */
	public List<TraceId> selectScatterTraceIdList(String applicationName, long from, long to, int limit);

	public List<SpanBo> selectTransactionMetadata(TransactionMetadataQuery query);
}
