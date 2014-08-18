package com.nhn.pinpoint.collector.receiver;

import com.nhn.pinpoint.collector.handler.Handler;
import com.nhn.pinpoint.thrift.dto.*;
import org.apache.thrift.TBase;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
public class UdpDispatchHandler extends AbstractDispatchHandler {

    @Autowired()
    @Qualifier("agentStatHandler")
    private Handler agentStatHandler;


    public UdpDispatchHandler() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    Handler getHandler(TBase<?, ?> tBase) {
        // code값을 기반으로 switch table로 바꾸면 눈꼽만큼 빨라짐.
        // FIXME (2014.08) Legacy - TAgentStats should not be sent over the wire.
        if (tBase instanceof TAgentStat || tBase instanceof TAgentStatBatch) {
            return agentStatHandler;
        }
        return null;
    }

}
