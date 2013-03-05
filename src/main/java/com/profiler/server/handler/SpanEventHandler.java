package com.profiler.server.handler;

import java.net.DatagramPacket;

import com.profiler.common.dto.thrift.SpanEvent;
import com.profiler.common.util.SpanEventUtils;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.ServiceType;
import com.profiler.server.dao.AgentIdApplicationIndexDao;
import com.profiler.server.dao.TerminalStatisticsDao;
import com.profiler.server.dao.TracesDao;

/**
 * subspan represent terminal spans.
 */
public class SpanEventHandler implements Handler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TracesDao traceDao;

    @Autowired
    private AgentIdApplicationIndexDao agentIdApplicationIndexDao;

    @Autowired
    private TerminalStatisticsDao terminalStatistics;

    @Override
    public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
        try {
            SpanEvent spanEvent = (SpanEvent) tbase;

            if (logger.isInfoEnabled()) {
                logger.info("Received SpanEvent={}", spanEvent);
            }

            String applicationName = agentIdApplicationIndexDao.selectApplicationName(spanEvent.getAgentId());

            if (applicationName == null) {
                logger.warn("Applicationname '{}' not found. Drop the log.", applicationName);
                return;
            } else {
                logger.info("Applicationname '{}' found. Write the log.", applicationName);
            }

            traceDao.insertEvent(applicationName, spanEvent);
            
            ServiceType serviceType = ServiceType.findServiceType(spanEvent.getServiceType());

			if (!serviceType.isRecordStatistics()) {
				return;
			}
            
            // if terminal update statistics
            int elapsed = spanEvent.getEndElapsed();
            boolean hasException = SpanEventUtils.hasException(spanEvent);
            
            // TODO 이제 타입구분안해도 됨. 대산에 destinationAddress를 추가로 업데이트 쳐야 될듯하다.
        	// TODO host로 spanEvent.getEndPoint()를 사용하는 것 변경
            terminalStatistics.update(applicationName, spanEvent.getDestinationId(), serviceType.getCode(), spanEvent.getEndPoint(), elapsed, hasException);
        } catch (Exception e) {
            logger.warn("SpanEvent handle error " + e.getMessage(), e);
        }
    }
}
