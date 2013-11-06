package com.nhn.pinpoint.web.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.nhn.pinpoint.web.vo.scatter.Dot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.nhn.pinpoint.web.dao.TraceDao;
import com.nhn.pinpoint.web.filter.Filter;
import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.web.vo.TransactionMetadataQuery;

/**
 * @author netspider
 * @author emeroad
 */
@Service
public class ScatterChartServiceImpl implements ScatterChartService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
	private ApplicationTraceIndexDao applicationTraceIndexDao;

	@Autowired
	private TraceDao traceDao;

	@Override
	public List<Dot> selectScatterData(String applicationName, long from, long to, int limit) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        return applicationTraceIndexDao.scanTraceScatter2(applicationName, from, to, limit);
	}

	@Override
	public List<Dot> selectScatterData(Collection<TransactionId> transactionIdList, String applicationName, Filter filter) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }

        final List<List<SpanBo>> traceList = traceDao.selectAllSpans(transactionIdList);

		List<Dot> list = new ArrayList<Dot>();

		for (List<SpanBo> trace : traceList) {
			if (!filter.include(trace)) {
				continue;
			}

			for (SpanBo span : trace) {
				if (applicationName.equals(span.getApplicationId())) {
                    TransactionId transactionId = new TransactionId(span.getTraceAgentId(), span.getTraceAgentStartTime(), span.getTraceTransactionSequence());
                    list.add(new Dot(transactionId, span.getCollectorAcceptTime(), span.getElapsed(), span.getErrCode(), span.getAgentId()));
				}
			}
		}

		return list;
	}

	/**
	 * scatter chart에서 선택한 점에 대한 정보를 조회 하는 메소드.
	 */
	@Override
	public List<SpanBo> selectTransactionMetadata(final TransactionMetadataQuery query) {
        if (query == null) {
            throw new NullPointerException("query must not be null");
        }
        final List<TransactionId> transactionIdList = query.getTransactionIdList();
        final List<List<SpanBo>> selectedSpans = traceDao.selectSpans(transactionIdList);

		List<SpanBo> result = new ArrayList<SpanBo>(query.size());

		// 조회된 녀석들 중에서 UUID, starttime, responseTime이 같은것들만 골라냄.
        int index = 0;
        for (List<SpanBo> spans : selectedSpans) {
            if (spans.size() == 0) {
                // 조회에 실패한 경우 span저장에 실패함.
                // skip한다.
            } else if (spans.size() == 1) {
                result.add(spans.get(0));
            } else {
                for (SpanBo span : spans) {
                    final TransactionMetadataQuery.QueryCondition queryCondition = query.getQueryConditionByIndex(index);

                    final TransactionId transactionId = new TransactionId(span.getTraceAgentId(), span.getTraceAgentStartTime(), span.getTraceTransactionSequence());
                    final TransactionMetadataQuery.QueryCondition key = new TransactionMetadataQuery.QueryCondition(transactionId, span.getCollectorAcceptTime(), span.getElapsed());
                    if (queryCondition.equals(key)) {
                        result.add(span);
                    }
                }
            }
            index++;
		}

		return result;
	}
}
