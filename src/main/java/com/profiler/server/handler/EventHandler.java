package com.profiler.server.handler;

import java.net.DatagramPacket;

import com.profiler.common.dto.thrift.Event;
import com.profiler.common.util.SubSpanUtils;
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
public class EventHandler implements Handler {

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
            Event event = (Event) tbase;

            if (logger.isInfoEnabled()) {
                logger.info("Received Event={}", event);
            }

            String applicationName = agentIdApplicationIndexDao.selectApplicationName(event.getAgentId());

            if (applicationName == null) {
                logger.warn("Applicationname '{}' not found. Drop the log.", applicationName);
                return;
            } else {
                logger.info("Applicationname '{}' found. Write the log.", applicationName);
            }

            traceDao.insertEvent(applicationName, event);
            
            ServiceType serviceType = ServiceType.findServiceType(event.getServiceType());

			if (!serviceType.isRecordStatistics()) {
				return;
			}
            
            // if terminal update statistics
            int elapsed = event.getEndElapsed();
            boolean hasException = SubSpanUtils.hasException(event);
            // 이제 타입구분안해도 됨. 대산에 destinationAddress를 추가로 업데이트 쳐야 될듯하다.
            if (serviceType.isRpcClient()) {
                terminalStatistics.update(applicationName, event.getDestinationId(), serviceType.getCode(), elapsed, hasException);
            } else {
                terminalStatistics.update(applicationName, event.getDestinationId(), serviceType.getCode(), elapsed, hasException);
            }
        } catch (Exception e) {
            logger.warn("Event handle error " + e.getMessage(), e);
        }
    }
}
