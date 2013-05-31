package com.profiler.server.handler;

import java.net.DatagramPacket;
import java.util.List;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.dto2.thrift.Span;
import com.nhn.pinpoint.common.dto2.thrift.SpanEvent;
import com.nhn.pinpoint.common.util.SpanEventUtils;
import com.profiler.server.dao.AgentIdApplicationIndexDao;
import com.profiler.server.dao.ApplicationMapStatisticsCalleeDao;
import com.profiler.server.dao.ApplicationMapStatisticsCallerDao;
import com.profiler.server.dao.ApplicationTraceIndexDao;
import com.profiler.server.dao.HostApplicationMapDao;
import com.profiler.server.dao.TraceIndexDao;
import com.profiler.server.dao.TracesDao;

public class SpanHandler implements Handler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TraceIndexDao traceIndexDao;

    @Autowired
    private TracesDao traceDao;

    @Autowired
    private ApplicationTraceIndexDao applicationTraceIndexDao;

    @Autowired
    private AgentIdApplicationIndexDao agentIdApplicationIndexDao;

	// @Autowired
	// private ClientStatisticsDao clientStatisticsDao;
    
    @Autowired
    private ApplicationMapStatisticsCallerDao applicationMapStatisticsCallerDao;
    
    @Autowired
    private ApplicationMapStatisticsCalleeDao applicationMapStatisticsCalleeDao;
    
    @Autowired
    private HostApplicationMapDao hostApplicationMapDao;
    
    public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {

        if (!(tbase instanceof Span)) {
            throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + this.getClass().getName());
        }

        try {
            Span span = (Span) tbase;

            if (logger.isInfoEnabled()) {
                logger.info("Received SPAN={}", span);
            }

            traceDao.insert(span);
            traceIndexDao.insert(span);
            applicationTraceIndexDao.insert(span);
            // businessTransactionStatistics.update(span);

			if (span.getParentSpanId() == -1) {
				// TODO error가 있으면 getErr값이 0보다 큰가??
				// clientStatisticsDao.update(span.getApplicationId(), ServiceType.CLIENT.getCode(), span.getElapsed(), span.getErr() > 0);
                applicationMapStatisticsCalleeDao.update(span.getApplicationName(), span.getServiceType(), span.getApplicationName(), ServiceType.CLIENT.getCode(), span.getEndPoint(), span.getElapsed(), span.getErr() > 0);
				applicationMapStatisticsCallerDao.update(span.getApplicationName(), ServiceType.CLIENT.getCode(), span.getApplicationName(), span.getServiceType(), span.getEndPoint(), span.getElapsed(), span.getErr() > 0);
			}

			// parentApplicationContext가 있으면 statistics정보를 저장한다.
			// 통계정보에 기반한 서버 맵을 그릴 때 WAS1 -> WAS2요청에서 WAS2를 호출한 application의 이름 즉, WAS1을 알아야한다. 
			if (span.getParentApplicationName() != null) {
				logger.debug("Received parent application name. {}", span.getParentApplicationName());
				// TODO 원래는 부모의 serviceType을 알아야 한다.
				// 여기에서는 그냥 부모는 모두 TOMCAT이라 가정하고 테스트.
				applicationMapStatisticsCallerDao.update(span.getParentApplicationName(), span.getParentApplicationType(), span.getApplicationName(), span.getServiceType(), span.getEndPoint(), span.getElapsed(), span.getErr() > 0);
			}
			
			// host application map 저장.
			// root span이 아닌 경우에만 profiler에서 acceptor host를 채워주게 되어있다.
			if (span.getAcceptorHost() != null) {
				hostApplicationMapDao.insert(span.getAcceptorHost(), span.getApplicationName(), span.getServiceType());
			}
			
            List<SpanEvent> spanEventList = span.getSpanEventList();
            if (spanEventList != null) {
                logger.info("handle spanEvent size:{}", spanEventList.size());
                // TODO 껀바이 껀인데. 나중에 뭔가 한번에 업데이트 치는걸로 변경해야 될듯.
                for (SpanEvent spanEvent : spanEventList) {
                    ServiceType serviceType = ServiceType.findServiceType(spanEvent.getServiceType());
					if(!serviceType.isRecordStatistics()) {
                        continue;
                    }

                    // if terminal update statistics
					int elapsed = spanEvent.getEndElapsed();
                    boolean hasException = SpanEventUtils.hasException(spanEvent);
                    
                    // 통계정보에 기반한 서버맵을 그리기 위한 정보 저장.
                    // 내가 호출한 정보 저장. (span이 호출한 spanevent)
					applicationMapStatisticsCalleeDao.update(spanEvent.getDestinationId(), serviceType.getCode(), span.getApplicationName(), span.getServiceType(), spanEvent.getEndPoint(), elapsed, hasException);

					// 나를 호출한 정보 저장 (spanevent를 호출한 span)
					applicationMapStatisticsCallerDao.update(span.getApplicationName(), span.getServiceType(), spanEvent.getDestinationId(), spanEvent.getServiceType(), span.getEndPoint(), elapsed, hasException);
					
                    // TODO 이제 타입구분안해도 됨. 대산에 destinationAddress를 추가로 업데이트 쳐야 될듯하다.
                	// TODO host로 spanEvent.getEndPoint()를 사용하는 것 변경
					
					// callee, caller statistics추가되면서 사용 안함.
                    // terminalStatistics.update(span.getApplicationId(), spanEvent.getDestinationId(), serviceType.getCode(), spanEvent.getEndPoint(), elapsed, hasException);
                }
            }
        } catch (Exception e) {
            logger.warn("Span handle error " + e.getMessage(), e);
        }
    }
}
