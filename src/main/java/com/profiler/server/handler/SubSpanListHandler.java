package com.profiler.server.handler;

import com.profiler.common.ServiceType;
import com.profiler.common.dto.thrift.SubSpan;
import com.profiler.common.dto.thrift.SubSpanList;
import com.profiler.server.dao.*;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.DatagramPacket;
import java.util.List;

/**
 *
 */
public class SubSpanListHandler implements Handler {

    private final Logger logger = LoggerFactory.getLogger(SubSpanHandler.class.getName());


    @Autowired
    private TracesDao traceDao;


    @Autowired
    private AgentIdApplicationIndexDao agentIdApplicationIndexDao;

    @Autowired
    private TerminalStatisticsDao terminalStatistics;

    @Override
    public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
        try {
            SubSpanList subSpanList = (SubSpanList) tbase;

            if (logger.isDebugEnabled()) {
                logger.debug("Received SubSpanList={}", subSpanList);
            }

            String applicationName = agentIdApplicationIndexDao.selectApplicationName(subSpanList.getAgentId());

            if (applicationName == null) {
                logger.warn("Applicationname '{}' not found. Drop the log.", applicationName);
                return;
            } else {
                logger.info("Applicationname '{}' found. Write the log.", applicationName);
            }


            traceDao.insertSubSpanList(applicationName, subSpanList);

            List<SubSpan> ssList = subSpanList.getSubSpanList();
            if (ssList != null) {
                logger.debug("SubSpanList Size:{}" + ssList.size());
                // TODO 껀바이 껀인데. 나중에 뭔가 한번에 업데이트 치는걸로 변경해야 될듯.
                for (SubSpan subSpan : ssList) {
                    ServiceType serviceType = ServiceType.parse(subSpan.getServiceType());
                    // if terminal update statistics
                    if (serviceType.isRpcClient()) {
                        terminalStatistics.update(applicationName, subSpan.getEndPoint(), serviceType.getCode());
                    } else {
                        terminalStatistics.update(applicationName, subSpan.getServiceName(), serviceType.getCode());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Span handle error " + e.getMessage(), e);
        }
    }
}