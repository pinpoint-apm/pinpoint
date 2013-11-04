package com.nhn.pinpoint.collector.handler;

import java.util.List;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.dao.TracesDao;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.thrift.dto.TSpanChunk;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;
import com.nhn.pinpoint.common.util.SpanEventUtils;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class SpanChunkHandler implements SimpleHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private TracesDao traceDao;

	@Autowired
	private StatisticsHandler statisticsHandler;

	@Override
	public void handler(TBase<?, ?> tbase) {

		if (!(tbase instanceof TSpanChunk)) {
			throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + this.getClass().getName());
		}

		try {
			TSpanChunk spanChunk = (TSpanChunk) tbase;

			if (logger.isDebugEnabled()) {
				logger.debug("Received SpanChunk={}", spanChunk);
			}

			traceDao.insertSpanChunk(spanChunk);

			List<TSpanEvent> spanEventList = spanChunk.getSpanEventList();
			if (spanEventList != null) {
				logger.debug("SpanChunk Size:{}", spanEventList.size());
				// TODO 껀바이 껀인데. 나중에 뭔가 한번에 업데이트 치는걸로 변경해야 될듯.
				for (TSpanEvent spanEvent : spanEventList) {
					ServiceType serviceType = ServiceType.findServiceType(spanEvent.getServiceType());

					if (!serviceType.isRecordStatistics()) {
						continue;
					}

					// if terminal update statistics
					int elapsed = spanEvent.getEndElapsed();
					boolean hasException = SpanEventUtils.hasException(spanEvent);

					/**
					 * 통계정보에 기반한 서버맵을 그리기 위한 정보 저장.
					 */
					// 내가 호출한 정보 저장. (span이 호출한 spanevent)
					statisticsHandler.updateCallee(spanEvent.getDestinationId(), serviceType.getCode(), spanChunk.getApplicationName(), spanChunk.getServiceType(), spanEvent.getEndPoint(), elapsed, hasException);

					// 나를 호출한 정보 저장 (spanevent를 호출한 span)
					statisticsHandler.updateCaller(spanChunk.getApplicationName(), spanChunk.getServiceType(), spanEvent.getDestinationId(), spanEvent.getServiceType(), spanChunk.getEndPoint(), elapsed, hasException);
				}
			}
		} catch (Exception e) {
			logger.warn("SpanChunk handle error Caused:{}", e.getMessage(), e);
		}
	}
}