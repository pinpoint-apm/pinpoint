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
            System.out.println("WARN Can't generate application map information.");
        } catch (Exception e) {
            logger.warn("SpanEvent handle error " + e.getMessage(), e);
        }
    }
}
