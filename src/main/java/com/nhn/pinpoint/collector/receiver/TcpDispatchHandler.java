package com.nhn.pinpoint.collector.receiver;

import com.nhn.pinpoint.collector.handler.RequestResponseHandler;
import com.nhn.pinpoint.collector.handler.SimpleHandler;
import com.nhn.pinpoint.thrift.dto.*;
import org.apache.thrift.TBase;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class TcpDispatchHandler extends AbstractDispatchHandler {

    @Autowired()
    @Qualifier("agentInfoHandler")
    private SimpleHandler agentInfoHandler;

    @Autowired()
    @Qualifier("sqlMetaDataHandler")
    private RequestResponseHandler sqlMetaDataHandler;

    @Autowired()
    @Qualifier("apiMetaDataHandler")
    private RequestResponseHandler apiMetaDataHandler;


    public TcpDispatchHandler() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }


    @Override
    RequestResponseHandler getRequestResponseHandler(TBase<?, ?> tBase) {
        if (tBase instanceof SqlMetaData) {
            return sqlMetaDataHandler;
        }
        if (tBase instanceof ApiMetaData) {
            return apiMetaDataHandler;
        }
        return null;
    }

    @Override
    SimpleHandler getSimpleHandler(TBase<?, ?> tBase) {

        if (tBase instanceof AgentInfo) {
            return agentInfoHandler;
        }

        return null;
    }
}
