package com.profiler.server.handler;

import com.profiler.common.ServiceType;
import com.profiler.common.dto.thrift.Span;
import com.profiler.common.dto.thrift.SubSpan;
import com.profiler.server.dao.*;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.DatagramPacket;

/**
 *
 */
public class SubSpanHandler implements Handler {

    private final Logger logger = LoggerFactory.getLogger(SubSpanHandler.class.getName());

    @Autowired
    private TraceIndexDao traceIndexDao;

    @Autowired
    private TracesDao traceDao;

    @Autowired
    private RootTraceIndexDaoDao rootTraceIndexDao;

    @Autowired
    private ApplicationTraceIndexDao applicationTraceIndexDao;

    @Autowired
    private AgentIdApplicationIndexDao agentIdApplicationIndexDao;

    @Autowired
    private TerminalStatisticsDao terminalStatistics;

    @Override
    public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
        try {
            SubSpan subSpan = (SubSpan) tbase;

            if (logger.isInfoEnabled()) {
                logger.info("Received SubSPAN=" + subSpan);
            }

            String applicationName = agentIdApplicationIndexDao.selectApplicationName(subSpan.getAgentId());

            if (applicationName == null) {
                logger.warn("Applicationname '{}' not found. Drop the log.", applicationName);
                return;
            } else {
                logger.info("Applicationname '{}' found. Write the log.", applicationName);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Found Applicationname={}", applicationName);
            }

            ServiceType serviceType = ServiceType.parse(subSpan.getServiceType());

            traceDao.insertTerminalSpan(applicationName, subSpan);

            // if terminal update statistics
//            terminalStatistics.update(applicationName, subSpan.getServiceName(), serviceType.getCode(), subSpan.getAgentId());


        } catch (Exception e) {
            logger.warn("Span handle error " + e.getMessage(), e);
        }
    }
}
