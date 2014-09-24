package com.nhn.pinpoint.web.dao;

import java.util.List;

import com.nhn.pinpoint.web.vo.LimitedScanResult;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.SelectedScatterArea;
import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.web.vo.scatter.Dot;

/**
 * @author emeroad
 * @author netspider
 */
public interface ApplicationTraceIndexDao {

	LimitedScanResult<List<TransactionId>> scanTraceIndex(String applicationName, Range range, int limit);

	LimitedScanResult<List<TransactionId>> scanTraceIndex(String applicationName, SelectedScatterArea range, int limit);

	List<Dot> scanTraceScatter(String applicationName, Range range, int limit);

	/**
	 * scatter chart에서 선택한 영역에 속하는 transaction 조회
	 * 
	 * @param applicationName
	 * @param area
	 * @param offsetTransactionId
	 * @param offsetTransactionElapsed
	 * @param limit
	 * @return
	 */
	List<Dot> scanTraceScatter(String applicationName, SelectedScatterArea area, TransactionId offsetTransactionId, int offsetTransactionElapsed, int limit);
}
