package com.nhn.pinpoint.collector.receiver;

import com.nhn.pinpoint.collector.handler.Handler;
import com.nhn.pinpoint.collector.handler.RequestResponseHandler;
import com.nhn.pinpoint.collector.handler.SimpleHandler;
import com.nhn.pinpoint.collector.util.AcceptedTimeService;
import com.nhn.pinpoint.common.dto2.thrift.*;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class DispatchHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired()
    @Qualifier("JVMDataHandler")
    private Handler jvmDataHandler;

    @Autowired()
    @Qualifier("spanHandler")
    private SimpleHandler spanDataHandler;

    @Autowired()
    @Qualifier("agentInfoHandler")
    private SimpleHandler agentInfoHandler;

    @Autowired()
    @Qualifier("agentStatHandler")
    private Handler agentStatHandler;
    
    @Autowired()
    @Qualifier("spanEventHandler")
    private SimpleHandler spanEventHandler;

    @Autowired()
    @Qualifier("spanChunkHandler")
    private SimpleHandler spanChunkHandler;

    @Autowired()
    @Qualifier("sqlMetaDataHandler")
    private RequestResponseHandler sqlMetaDataHandler;

    @Autowired()
    @Qualifier("apiMetaDataHandler")
    private RequestResponseHandler apiMetaDataHandler;

    @Autowired
    private AcceptedTimeService acceptedTimeService;

    public DispatchHandler() {
    }


    public TBase dispatch(TBase<?, ?> tBase, byte[] packet, int offset, int length) {
        // accepted time 마크
        acceptedTimeService.accept();
        // TODO 수정시 dispatch table은 자동으로 바뀌게 변경해도 될듯하다.
        SimpleHandler simpleHandler = getSimpleHandler(tBase);
        if (simpleHandler != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("simpleHandler name:{}", simpleHandler.getClass().getName());
            }
            simpleHandler.handler(tBase);
            return null;
        }

        Handler handler = getHandler(tBase);
        if (handler != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("handler name:{}", handler.getClass().getName());
            }
            handler.handler(tBase, packet, offset, length);
            return null;
        }

        RequestResponseHandler requestResponseHandler = getRequestResponseHandler(tBase);
        if (requestResponseHandler != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("requestResponseHandler name:{}", requestResponseHandler.getClass().getName());
            }
            return requestResponseHandler.handler(tBase);
        }

        throw new UnsupportedOperationException("Handler not found. Unknown type of data received. tBase=" + tBase);
    }

    private Handler getHandler(TBase<?, ?> tBase) {

        // code값을 기반으로 switch table로 바꾸면 눈꼽만큼 빨라짐.
        if (tBase instanceof JVMInfoThriftDTO) {
            return jvmDataHandler;
        }
        if (tBase instanceof AgentStat) {
            return agentStatHandler;
        }
        return null;
    }

    public RequestResponseHandler getRequestResponseHandler(TBase<?, ?> tBase) {
        if (tBase instanceof SqlMetaData) {
            return sqlMetaDataHandler;
        }
        if (tBase instanceof ApiMetaData) {
            return apiMetaDataHandler;
        }
        return null;
    }

    public SimpleHandler getSimpleHandler(TBase<?, ?> tBase) {
        if (tBase instanceof Span) {
            return spanDataHandler;
        }
        if (tBase instanceof AgentInfo) {
            return agentInfoHandler;
        }
        if (tBase instanceof SpanEvent) {
            return spanEventHandler;
        }
        if (tBase instanceof SpanChunk) {
            return spanChunkHandler;
        }

        return null;
    }
}
