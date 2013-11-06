package com.nhn.pinpoint.web.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.nhn.pinpoint.web.vo.scatter.Dot;
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
	public List<Dot> selectScatterData(Collection<TransactionId> traceIds, String applicationName, Filter filter) {
        if (traceIds == null) {
            throw new NullPointerException("traceIds must not be null");
        }
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }

        List<List<SpanBo>> traceList = traceDao.selectAllSpans(traceIds);

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
	public List<SpanBo> selectTransactionMetadata(TransactionMetadataQuery query) {
        if (query == null) {
            throw new NullPointerException("query must not be null");
        }

        List<List<SpanBo>> selectedSpans = traceDao.selectSpans(query.getTraceIds());

		List<SpanBo> result = new ArrayList<SpanBo>(query.size());

		// 조회된 녀석들 중에서 UUID, starttime, responseTime이 같은것들만 골라냄.
		for (List<SpanBo> spans : selectedSpans) {
			for (SpanBo span : spans) {
				// check UUID and time
				if (query.isExists(span.getTraceAgentId(), span.getTraceAgentStartTime(), span.getTraceTransactionSequence(), span.getCollectorAcceptTime(), span.getElapsed())) {
					result.add(span);
				}
			}
		}

		// TODO 일단 임시로...
		Collections.sort(result, spanComparator);

		return result;
	}

	private final Comparator<SpanBo> spanComparator = new Comparator<SpanBo>() {
		@Override
		public int compare(SpanBo o1, SpanBo o2) {
			if (o1.getErrCode() != 0 && o2.getErrCode() != 0) {
				return o2.getElapsed() - o1.getElapsed();
			} else if (o1.getErrCode() != 0) {
				return -1;
			} else if (o2.getErrCode() != 0) {
				return 1;
			} else {
				return o2.getElapsed() - o1.getElapsed();
			}
		}
	};
}
