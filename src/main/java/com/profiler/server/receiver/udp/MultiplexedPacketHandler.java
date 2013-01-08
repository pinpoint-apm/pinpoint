package com.profiler.server.receiver.udp;

import java.net.DatagramPacket;

import com.profiler.common.dto.thrift.*;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import com.profiler.common.util.HeaderTBaseDeserializer;
import com.profiler.common.util.TBaseLocator;
import com.profiler.server.handler.Handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class MultiplexedPacketHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private TBaseLocator locator;

    @Autowired()
    @Qualifier("JvmDataHandler")
    private Handler jvmDataHandler;

    @Autowired()
    @Qualifier("SpanHandler")
    private Handler spanDataHandler;

    @Autowired()
    @Qualifier("AgentInfoHandler")
    private Handler agentInfoHandler;

    @Autowired()
    @Qualifier("SubSpanHandler")
    private Handler subSpanHandler;

    @Autowired()
    @Qualifier("SubSpanListHandler")
    private Handler subSpanListHandler;

    @Autowired()
    @Qualifier("SqlMetaDataHandler")
    private Handler sqlMetaDataHandler;

    public MultiplexedPacketHandler() {
    }

    public void handlePacket(DatagramPacket packet) {
        HeaderTBaseDeserializer deserializer = new HeaderTBaseDeserializer();
        try {
            TBase<?, ?> tBase = deserializer.deserialize(locator, packet.getData());
            dispatch(tBase, packet);
        } catch (TException e) {
            logger.warn("packet serialize error " + e.getMessage(), e);
        }
    }

    private void dispatch(TBase<?, ?> tBase, DatagramPacket datagramPacket) {
        Handler handler = getHandler(tBase);
        if (logger.isDebugEnabled()) {
            logger.debug("handler name:" + handler.getClass().getName());
        }

        handler.handler(tBase, datagramPacket);
    }

    private Handler getHandler(TBase<?, ?> tBase) {
        if (tBase instanceof JVMInfoThriftDTO) {
            return jvmDataHandler;
        }
        if (tBase instanceof Span) {
            return spanDataHandler;
        }
        if (tBase instanceof AgentInfo) {
            return agentInfoHandler;
        }
        if (tBase instanceof SubSpan) {
            return subSpanHandler;
        }
        if (tBase instanceof SubSpanList) {
            return subSpanListHandler;
        }
        if (tBase instanceof SqlMetaData) {
            return sqlMetaDataHandler;
        }
        logger.warn("Unknown type of data received. data=" + tBase);
        throw new UnsupportedOperationException();
    }

}
