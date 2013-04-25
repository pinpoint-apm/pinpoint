package com.nhn.hippo.web.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.hippo.web.dao.ApplicationTraceIndexDao;
import com.nhn.hippo.web.dao.TraceDao;
import com.nhn.hippo.web.vo.TransactionMetadataQuery;
import com.nhn.hippo.web.vo.scatter.Dot;
import com.profiler.common.bo.SpanBo;

/**
 * @author netspider
 */
@Service
public class ScatterChartServiceImpl implements ScatterChartService {

	@Autowired
	private ApplicationTraceIndexDao applicationTraceIndexDao;

	@Autowired
	private TraceDao traceDao;

	@Override
	public List<Dot> selectScatterData(String applicationName, long from, long to, int limit) {
		return applicationTraceIndexDao.scanTraceScatter2(applicationName, from, to, limit);
	}

	/**
	 * scatter chart에서 선택한 점에 대한 정보를 조회 하는 메소드.
	 */
	@Override
	public List<SpanBo> selectTransactionMetadata(TransactionMetadataQuery query) {
		List<List<SpanBo>> selectedSpans = traceDao.selectSpans(query.getTraceIds());

		List<SpanBo> result = new ArrayList<SpanBo>(query.size());

		// 조회된 녀석들 중에서 UUID, starttime, responseTime이 같은것들만 골라냄.
		for (List<SpanBo> spans : selectedSpans) {
			for (SpanBo span : spans) {
				// check UUID and time
				if (query.isExists(span.getMostTraceId(), span.getLeastTraceId(), span.getCollectorAcceptTime(), span.getElapsed())) {
					result.add(span);
				}
			}
		}

		// TODO 일단 임시로...
		Collections.sort(result, new Comparator<SpanBo>() {
			@Override
			public int compare(SpanBo o1, SpanBo o2) {
				if (o1.getException() != 0 && o2.getException() != 0) {
					return o2.getElapsed() - o1.getElapsed();
				} else if (o1.getException() != 0) {
					return -1;
				} else if (o2.getException() != 0) {
					return 1;
				} else {
					return o2.getElapsed() - o1.getElapsed();
				}
			}
		});

		return result;
	}
}
