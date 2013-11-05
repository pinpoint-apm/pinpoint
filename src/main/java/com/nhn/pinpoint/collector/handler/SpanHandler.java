package com.nhn.pinpoint.collector.handler;

import java.util.List;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.dao.ApplicationTraceIndexDao;
import com.nhn.pinpoint.collector.dao.HostApplicationMapDao;
import com.nhn.pinpoint.collector.dao.TracesDao;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.thrift.dto.TSpan;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;
import com.nhn.pinpoint.common.util.SpanEventUtils;
import org.springframework.stereotype.Service;

/**
 * @author emeroad
 */
@Service
public class SpanHandler implements SimpleHandler {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private TracesDao traceDao;

	@Autowired
	private ApplicationTraceIndexDao applicationTraceIndexDao;

	@Autowired
	private StatisticsHandler statisticsHandler;

	@Autowired
	private HostApplicationMapDao hostApplicationMapDao;

	public void handler(TBase<?, ?> tbase) {

		if (!(tbase instanceof TSpan)) {
			throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + this.getClass().getName());
		}

		try {
			TSpan span = (TSpan) tbase;
			if (logger.isDebugEnabled()) {
				logger.debug("Received SPAN={}", span);
			}

            traceDao.insert(span);
//			traceIndexDao.insert(span);
			applicationTraceIndexDao.insert(span);

			if (span.getParentSpanId() == -1) {
				// TODO error가 있으면 getErr값이 0보다 큰가??
//				statisticsHandler.updateCallee(span.getApplicationName(), span.getServiceType(), span.getApplicationName(), ServiceType.CLIENT.getCode(), span.getEndPoint(), span.getElapsed(), span.getErr() > 0);
//				statisticsHandler.updateCaller(span.getApplicationName(), ServiceType.CLIENT.getCode(), span.getApplicationName(), span.getServiceType(), span.getEndPoint(), span.getElapsed(), span.getErr() > 0);
				// FIXME 테스트용. host값에 agentId를 입력.
				statisticsHandler.updateCallee(span.getApplicationName(), span.getServiceType(), span.getApplicationName(), ServiceType.CLIENT.getCode(), span.getAgentId(), span.getElapsed(), span.getErr() > 0);
				statisticsHandler.updateCaller(span.getApplicationName(), ServiceType.CLIENT.getCode(), span.getApplicationName(), span.getServiceType(), span.getAgentId(), span.getElapsed(), span.getErr() > 0);
			}

			// parentApplicationContext가 있으면 statistics정보를 저장한다.
			// 통계정보에 기반한 서버 맵을 그릴 때 WAS1 -> WAS2요청에서 WAS2를 호출한 application의 이름
			// 즉, WAS1을 알아야한다.
			if (span.getParentApplicationName() != null) {
				logger.debug("Received parent application name. {}", span.getParentApplicationName());
				// TODO 원래는 부모의 serviceType을 알아야 한다.
				// 여기에서는 그냥 부모는 모두 TOMCAT이라 가정하고 테스트.
				statisticsHandler.updateCaller(span.getParentApplicationName(), span.getParentApplicationType(), span.getApplicationName(), span.getServiceType(), span.getAgentId(), span.getElapsed(), span.getErr() > 0);
				// statisticsHandler.updateCaller(span.getParentApplicationName(), span.getParentApplicationType(), span.getApplicationName(), span.getServiceType(), span.getEndPoint(), span.getElapsed(), span.getErr() > 0);
			}

			// host application map 저장.
			// root span이 아닌 경우에만 profiler에서 acceptor host를 채워주게 되어있다.
			if (span.getAcceptorHost() != null) {
				hostApplicationMapDao.insert(span.getAcceptorHost(), span.getApplicationName(), span.getServiceType());
			}

			List<TSpanEvent> spanEventList = span.getSpanEventList();
			if (spanEventList != null) {
				logger.debug("handle spanEvent size:{}", spanEventList.size());
				// TODO 껀바이 껀인데. 나중에 뭔가 한번에 업데이트 치는걸로 변경해야 될듯.
				for (TSpanEvent spanEvent : spanEventList) {
					ServiceType serviceType = ServiceType.findServiceType(spanEvent.getServiceType());
					if (!serviceType.isRecordStatistics()) {
						continue;
					}

					// if terminal update statistics
					int elapsed = spanEvent.getEndElapsed();
					boolean hasException = SpanEventUtils.hasException(spanEvent);

					// 통계정보에 기반한 서버맵을 그리기 위한 정보 저장.
					// 내가 호출한 정보 저장. (span이 호출한 spanevent)
					statisticsHandler.updateCallee(spanEvent.getDestinationId(), serviceType.getCode(), span.getApplicationName(), span.getServiceType(), spanEvent.getEndPoint(), elapsed, hasException);

					// 나를 호출한 정보 저장 (spanevent를 호출한 span)
					statisticsHandler.updateCaller(span.getApplicationName(), span.getServiceType(), spanEvent.getDestinationId(), spanEvent.getServiceType(), span.getEndPoint(), elapsed, hasException);

					// TODO 이제 타입구분안해도 됨. 대산에 destinationAddress를 추가로 업데이트 쳐야
					// 될듯하다.
					// TODO host로 spanEvent.getEndPoint()를 사용하는 것 변경
				}
			}
		} catch (Exception e) {
			logger.warn("Span handle error. Caused:{}. Span:{}",e.getMessage(), tbase, e);
		}
	}
}
