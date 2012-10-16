package com.profiler.server.handler;

import java.net.DatagramPacket;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.profiler.common.dto.thrift.AgentInfo;
import com.profiler.common.dto.thrift.Span;

public class AgentInfoHandler implements Handler {

    private final Logger logger = LoggerFactory.getLogger(AgentInfoHandler.class.getName());

    public void handler(TBase<?, ?> tbase, DatagramPacket datagramPacket) {
        assert (tbase instanceof Span);

        try {
            AgentInfo agentInfo = (AgentInfo) tbase;

            if (logger.isInfoEnabled()) {
                logger.info("Received AgentInfo=" + agentInfo);
            }
        } catch (Exception e) {
            logger.warn("Span handle error " + e.getMessage(), e);
        }
    }
}
