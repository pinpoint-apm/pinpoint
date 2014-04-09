package com.nhn.pinpoint.collector.handler;

import com.nhn.pinpoint.thrift.dto.TAgentStat;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.pinpoint.collector.StatServer;
import com.nhn.pinpoint.collector.dao.AgentStatDao;
import com.nhn.pinpoint.thrift.io.Header;
import com.nhn.pinpoint.thrift.io.PacketUtils;

/**
 * @author emeroad
 */
@Service("agentStatHandler")
public class AgentStatHandler implements Handler {

	private final Logger logger = LoggerFactory.getLogger(AgentStatHandler.class.getName());


	@Autowired
	private AgentStatDao agentStatDao;

	public void handle(TBase<?, ?> tbase, byte[] packet, int offset, int length) {
        if (!(tbase instanceof TAgentStat)) {
            throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + this.getClass().getName());
        }

        try {
        	TAgentStat dto = (TAgentStat) tbase;
        	
            if (logger.isDebugEnabled()) {
                logger.debug("Received AgentStat={}", dto);
            }
            
			// HBase에 기록한다.
            byte[] value = PacketUtils.sliceData(packet, Header.HEADER_SIZE, length);
			agentStatDao.insert(dto, value);
        } catch (Exception e) {
            logger.warn("AgentStat handle error. Caused:{}", e.getMessage(), e);
        }
	}
}
