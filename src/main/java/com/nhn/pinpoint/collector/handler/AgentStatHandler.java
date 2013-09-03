package com.nhn.pinpoint.collector.handler;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.pinpoint.collector.StatServer;
import com.nhn.pinpoint.collector.dao.AgentStatDao;
import com.nhn.pinpoint.common.dto2.Header;
import com.nhn.pinpoint.common.dto2.thrift.AgentStat;
import com.nhn.pinpoint.common.io.PacketUtils;

@Service("agentStatHandler")
public class AgentStatHandler implements Handler {

	private final Logger logger = LoggerFactory.getLogger(AgentStatHandler.class.getName());

	@Autowired
	private StatServer statServer;
	
	@Autowired
	private AgentStatDao agentStatDao;

	public void handler(TBase<?, ?> tbase, byte[] packet, int offset, int length) {
        if (!(tbase instanceof AgentStat)) {
            throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + this.getClass().getName());
        }

        try {
        	AgentStat dto = (AgentStat) tbase;
        	
            if (logger.isDebugEnabled()) {
                logger.debug("Received AgentStat={}", dto);
            }
            
            if (statServer != null) {
            	// 메모리 스토어에 snapshot을 저장한다.
            	statServer.getStore().store(dto);
            }
            
			// HBase에 기록한다.
            byte[] value = PacketUtils.sliceData(packet, Header.HEADER_SIZE, length);
			agentStatDao.insert(dto, value);
        } catch (Exception e) {
            logger.warn("AgentStat handle error. Caused:{}", e.getMessage(), e);
        }
	}
}
