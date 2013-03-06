package com.profiler.server.handler;

import java.net.DatagramPacket;
import java.util.List;

import com.profiler.common.dto.thrift.SpanEvent;
import com.profiler.common.dto.thrift.SpanChunk;
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
 *
 */
public class SpanChunkHandler implements Handler {

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
            SpanChunk spanChunk = (SpanChunk) tbase;

            if (logger.isDebugEnabled()) {
                logger.debug("Received SpanChunk={}", spanChunk);
            }

            String applicationName = spanChunk.getApplicationId();
            traceDao.insertSpanChunk(applicationName, spanChunk);

            List<SpanEvent> spanEventList = spanChunk.getSpanEventList();
            if (spanEventList != null) {
                logger.debug("SpanChunk Size:{}", spanEventList.size());
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
                    terminalStatistics.update(applicationName, spanEvent.getDestinationId(), serviceType.getCode(), spanEvent.getEndPoint(), elapsed, hasException);
                }
            }
        } catch (Exception e) {
            logger.warn("SpanChunk handle error " + e.getMessage(), e);
        }
    }
}