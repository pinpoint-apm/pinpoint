package com.profiler.server.handler;

import java.net.DatagramPacket;

import com.profiler.common.util.SubSpanUtils;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.ServiceType;
import com.profiler.common.dto.thrift.SubSpan;
import com.profiler.server.dao.AgentIdApplicationIndexDao;
import com.profiler.server.dao.TerminalStatisticsDao;
import com.profiler.server.dao.TracesDao;

/**
 * subspan represent terminal spans.
 */
public class SubSpanHandler implements Handler {

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
            SubSpan subSpan = (SubSpan) tbase;

            if (logger.isInfoEnabled()) {
                logger.info("Received SubSPAN={}", subSpan);
            }

            String applicationName = agentIdApplicationIndexDao.selectApplicationName(subSpan.getAgentId());

            if (applicationName == null) {
                logger.warn("Applicationname '{}' not found. Drop the log.", applicationName);
                return;
            } else {
                logger.info("Applicationname '{}' found. Write the log.", applicationName);
            }


            traceDao.insertSubSpan(applicationName, subSpan);

            ServiceType serviceType = ServiceType.findServiceType(subSpan.getServiceType());
            
            // if terminal update statistics
            int elapsed = subSpan.getEndElapsed();
            boolean hasException = SubSpanUtils.hasException(subSpan);
            if (serviceType.isRpcClient()) {
                terminalStatistics.update(applicationName, subSpan.getEndPoint(), serviceType.getCode(), elapsed, hasException);
            } else {
                terminalStatistics.update(applicationName, subSpan.getServiceName(), serviceType.getCode(), elapsed, hasException);
            }
        } catch (Exception e) {
            logger.warn("SubSpan handle error " + e.getMessage(), e);
        }
    }
}
