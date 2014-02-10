package com.nhn.pinpoint.web.service;

import java.util.Collection;
import java.util.List;

import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.web.filter.Filter;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.web.vo.TransactionMetadataQuery;
import com.nhn.pinpoint.web.vo.scatter.Dot;

public interface ScatterChartService {

	/**
	 * 필터를 사용한 검색.
	 * 
	 * @param traceIds
	 * @param applicationName
	 * @param filter
	 * @return
	 */
	public List<Dot> selectScatterData(Collection<TransactionId> traceIds, String applicationName, Filter filter);

	/**
	 * 전체 데이터 검색.
	 * 
	 * @param applicationName
	 * @param range
	 * @param limit
	 * @return
	 */
	public List<Dot> selectScatterData(String applicationName, Range range, int limit);

	/**
	 * scatter dot을 limit 개수만큼 잘라서 조회하기 위해서 사용된다.
	 * 
	 * @param applicationName
	 * @param from
	 * @param to
	 * @param limit
	 * @return
	 */
//	public List<TransactionId> selectScatterTraceIdList(String applicationName, long from, long to, int limit);

	public List<SpanBo> selectTransactionMetadata(TransactionMetadataQuery query);
}
