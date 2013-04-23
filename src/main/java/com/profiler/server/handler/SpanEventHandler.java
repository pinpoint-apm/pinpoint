package com.profiler.server.handler;

import java.net.DatagramPacket;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.ServiceType;
import com.profiler.common.dto2.thrift.SpanEvent;
import com.profiler.common.util.SpanEventUtils;
import com.profiler.server.dao.ApplicationMapStatisticsCalleeDao;
import com.profiler.server.dao.ApplicationMapStatisticsCallerDao;
import com.profiler.server.dao.TracesDao;

/**
 * subspan represent terminal spans.
 */
public class SpanEventHandler implements Handler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TracesDao traceDao;

	@Autowired
	private ApplicationMapStatisticsCallerDao applicationMapStatisticsCallerDao;

	@Autowired
	private ApplicationMapStatisticsCalleeDao applicationMapStatisticsCalleeDao;

    @Override
    public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {

        if (!(tbase instanceof SpanEvent)) {
            throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + this.getClass().getName());
        }

        try {
            SpanEvent spanEvent = (SpanEvent) tbase;

            if (logger.isInfoEnabled()) {
                logger.info("Received SpanEvent={}", spanEvent);
            }


            traceDao.insertEvent(spanEvent);
            
            ServiceType serviceType = ServiceType.findServiceType(spanEvent.getServiceType());

			if (!serviceType.isRecordStatistics()) {
				return;
			}
            
            // if terminal update statistics
            int elapsed = spanEvent.getEndElapsed();
            boolean hasException = SpanEventUtils.hasException(spanEvent);
            
            System.out.println("I am SpanEventHandler");
            
            // TODO update application map statistics
            // 내가 호출한 정보 저장. (span이 호출한 spanevent)
//			applicationMapStatisticsCallerDao.update(spanEvent.getApplicationId(), spanEvent.getDestinationId(), serviceType.getCode(), spanEvent.getEndPoint(), elapsed, hasException);
            
			// 나를 호출한 정보 저장 (spanevent를 호출한 span)
			// applicationMapStatisticsCalleeDao.update(spanEvent.getDestinationId(), spanEvent.getApplicationId(), span.getServiceType(), span.getEndPoint(), elapsed, hasException);
			
            // TODO 이제 타입구분안해도 됨. 대산에 destinationAddress를 추가로 업데이트 쳐야 될듯하다.
        	// TODO host로 spanEvent.getEndPoint()를 사용하는 것 변경
            
            // callee, caller statistics추가되면서 사용 안함.
            // terminalStatistics.update(spanEvent.getApplicationId(), spanEvent.getDestinationId(), serviceType.getCode(), spanEvent.getEndPoint(), elapsed, hasException);
        } catch (Exception e) {
            logger.warn("SpanEvent handle error " + e.getMessage(), e);
        }
    }
}
