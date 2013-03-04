package com.profiler.server.handler;

import java.net.DatagramPacket;
import java.util.List;

import com.profiler.common.util.SpanEventUtils;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.ServiceType;
import com.profiler.common.dto.thrift.Span;
import com.profiler.common.dto.thrift.SpanEvent;
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

            final String applicationName = agentIdApplicationIndexDao.selectApplicationName(span.getAgentId());
            if (applicationName == null) {
                logger.warn("ApplicationName '{}' not found. Drop the log.", applicationName);
                return;
            } else {
                logger.debug("ApplicationName '{}' found. Write the log.", applicationName);
            }

            traceDao.insert(applicationName, span);
            traceIndexDao.insert(span);
            applicationTraceIndexDao.insert(applicationName, span);
            businessTransactionStatistics.update(applicationName, span);

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
                    // 이제 타입구분안해도 됨. 대산에 destinationAddress를 추가로 업데이트 쳐야 될듯하다.
                    if (serviceType.isRpcClient()) {
                        terminalStatistics.update(applicationName, spanEvent.getDestinationId(), serviceType.getCode(), elapsed, hasException);
                    } else {
                        terminalStatistics.update(applicationName, spanEvent.getDestinationId(), serviceType.getCode(), elapsed, hasException);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Span handle error " + e.getMessage(), e);
        }
    }
}
