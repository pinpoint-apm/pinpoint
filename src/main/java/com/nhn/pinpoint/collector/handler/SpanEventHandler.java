package com.nhn.pinpoint.collector.handler;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.dao.TracesDao;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.thrift.dto.SpanEvent;
import com.nhn.pinpoint.common.util.SpanEventUtils;
import org.springframework.stereotype.Service;

/**
 * subspan represent terminal spans.
 */
@Service
public class SpanEventHandler implements SimpleHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private TracesDao traceDao;

	@Autowired
	private StatisticsHandler statisticsHandler;

	@Override
	public void handler(TBase<?, ?> tbase) {

		if (!(tbase instanceof SpanEvent)) {
			throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + this.getClass().getName());
		}

		try {
			SpanEvent spanEvent = (SpanEvent) tbase;

			if (logger.isDebugEnabled()) {
				logger.debug("Received SpanEvent={}", spanEvent);
			}

			traceDao.insertEvent(spanEvent);

			ServiceType serviceType = ServiceType.findServiceType(spanEvent.getServiceType());

			if (!serviceType.isRecordStatistics()) {
				return;
			}

			// if terminal update statistics
			int elapsed = spanEvent.getEndElapsed();
			boolean hasException = SpanEventUtils.hasException(spanEvent);

			/**
			 * 통계정보에 기반한 서버맵을 그리기 위한 정보 저장.
			 */

			// application으로 들어오는 통계 정보 저장.
			statisticsHandler.updateApplication(spanEvent.getDestinationId(), serviceType.getCode(), spanEvent.getEndPoint(), elapsed, hasException);

			// 내가 호출한 정보 저장. (span이 호출한 spanevent)
			statisticsHandler.updateCallee(spanEvent.getDestinationId(), serviceType.getCode(), spanEvent.getAgentKey().getApplicationName(), spanEvent.getParentServiceType(), spanEvent.getEndPoint(), elapsed, hasException);

			// 나를 호출한 정보 저장 (spanevent를 호출한 span)
			statisticsHandler.updateCaller(spanEvent.getAgentKey().getApplicationName(), spanEvent.getParentServiceType(), spanEvent.getDestinationId(), spanEvent.getServiceType(), spanEvent.getParentEndPoint(), elapsed, hasException);
		} catch (Exception e) {
			logger.warn("SpanEvent handle error. Caused:{} ", e.getMessage(), e);
		}
	}
}
