package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.SelectedScatterArea;
import com.navercorp.pinpoint.web.vo.TransactionId;
import com.navercorp.pinpoint.web.vo.TransactionMetadataQuery;
import com.navercorp.pinpoint.web.vo.scatter.Dot;

import java.util.Collection;
import java.util.List;

public interface ScatterChartService {

    /**
     * 필터를 사용한 검색.
     *
     * @param traceIds
     * @param applicationName
     * @param filter
     * @return
     */
    List<Dot> selectScatterData(Collection<TransactionId> traceIds, String applicationName, Filter filter);

    /**
     * 전체 데이터 검색.
     *
     * @param applicationName
     * @param range
     * @param limit
     * @return
     */
    List<Dot> selectScatterData(String applicationName, Range range, int limit);

    /**
     * @param applicationName
     * @param area
     * @param offsetTransactionId
     * @param offsetTransactionElapsed
     * @param limit
     * @return
     */
    List<Dot> selectScatterData(String applicationName, SelectedScatterArea area, TransactionId offsetTransactionId, int offsetTransactionElapsed, int limit);

    /**
     * scatter dot을 limit 개수만큼 잘라서 조회하기 위해서 사용된다.
     *
     * @param applicationName
     * @param from
     * @param to
     * @param limit
     * @return
     */
//  List<TransactionId> selectScatterTraceIdList(String applicationName, long from, long to, int limit);
    List<SpanBo> selectTransactionMetadata(TransactionMetadataQuery query);
}
