package com.profiler.server.handler;

import java.net.DatagramPacket;
import java.util.List;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.ServiceType;
import com.profiler.common.dto.thrift.Span;
import com.profiler.common.dto.thrift.SubSpan;
import com.profiler.server.dao.AgentIdApplicationIndexDao;
import com.profiler.server.dao.ApplicationTraceIndexDao;
import com.profiler.server.dao.BusinessTransactionStatisticsDao;
import com.profiler.server.dao.TerminalStatisticsDao;
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

    @Autowired
    private TerminalStatisticsDao terminalStatistics;

    @Autowired
    private BusinessTransactionStatisticsDao businessTransactionStatistics;
    
    public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
        assert (tbase instanceof Span);

        try {
            Span span = (Span) tbase;

            if (logger.isInfoEnabled()) {
                logger.info("Received SPAN={}", span);
            }

            String applicationName = agentIdApplicationIndexDao.selectApplicationName(span.getAgentId());

            if (applicationName == null) {
                logger.warn("Applicationname '{}' not found. Drop the log.", applicationName);
                return;
            } else {
                logger.info("Applicationname '{}' found. Write the log.", applicationName);
            }

            traceDao.insert(applicationName, span);

			// (indexing root span)
			// if (span.getParentSpanId() == -1) {
			// rootTraceIndexDao.insert(span);
			// }

            // indexing non-terminal span
            ServiceType serviceType = ServiceType.parse(span.getServiceType());
            if (serviceType.isIndexable()) {
                traceIndexDao.insert(span);
                applicationTraceIndexDao.insert(applicationName, span);
                businessTransactionStatistics.update(applicationName, span);
            } else {
                logger.debug("Skip writing index. '{}'", span);
            }

            List<SubSpan> subSpanList = span.getSubSpanList();
            if (subSpanList != null) {
                logger.info("handle subSpan size:{}", subSpanList.size());
                // TODO 껀바이 껀인데. 나중에 뭔가 한번에 업데이트 치는걸로 변경해야 될듯.
                for (SubSpan subSpan : subSpanList) {
                    ServiceType subSpanServiceType = ServiceType.parse(subSpan.getServiceType());
                    
					// skip
					if (subSpanServiceType == ServiceType.INTERNAL_METHOD) {
						continue;
					}
					
                    // if terminal update statistics
					int elapsed = subSpan.getEndElapsed();
					
                    if (subSpanServiceType.isRpcClient()) {
                        terminalStatistics.update(applicationName, subSpan.getEndPoint(), serviceType.getCode(), elapsed, subSpan.isErr());
                    } else {
                        terminalStatistics.update(applicationName, subSpan.getServiceName(), serviceType.getCode(), elapsed, subSpan.isErr());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Span handle error " + e.getMessage(), e);
        }
    }
}
