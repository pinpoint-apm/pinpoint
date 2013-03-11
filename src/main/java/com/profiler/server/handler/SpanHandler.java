package com.profiler.server.handler;

import java.net.DatagramPacket;
import java.util.List;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.ServiceType;
import com.profiler.common.dto.thrift.Span;
import com.profiler.common.dto.thrift.SpanEvent;
import com.profiler.common.util.SpanEventUtils;
import com.profiler.server.dao.AgentIdApplicationIndexDao;
import com.profiler.server.dao.ApplicationTraceIndexDao;
import com.profiler.server.dao.BusinessTransactionStatisticsDao;
import com.profiler.server.dao.ClientStatisticsDao;
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
    
    @Autowired
    private ClientStatisticsDao clientStatisticsDao;
    
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
            businessTransactionStatistics.update(span);

			if (span.getParentSpanId() == -1) {
				// TODO error가 있으면 getErr값이 0보다 큰가??
				clientStatisticsDao.update(span.getApplicationId(), ServiceType.CLIENT.getCode(), span.getElapsed(), span.getErr() > 0);
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
                    
                    // TODO 이제 타입구분안해도 됨. 대산에 destinationAddress를 추가로 업데이트 쳐야 될듯하다.
                	// TODO host로 spanEvent.getEndPoint()를 사용하는 것 변경
                    terminalStatistics.update(span.getApplicationId(), spanEvent.getDestinationId(), serviceType.getCode(), spanEvent.getEndPoint(), elapsed, hasException);
                }
            }
        } catch (Exception e) {
            logger.warn("Span handle error " + e.getMessage(), e);
        }
    }
}
