package com.navercorp.pinpoint.collector.receiver;

import com.navercorp.pinpoint.collector.handler.AgentEventHandler;
import com.navercorp.pinpoint.collector.handler.AgentStatHandlerV2;
import com.navercorp.pinpoint.collector.handler.BusinessLogHandler;
import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import com.navercorp.pinpoint.thrift.dto.TBusinessLog;
import com.navercorp.pinpoint.thrift.dto.TBusinessLogBatch;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mockito.Mockito.verify;

/**
 * Created by suny on 2018/2/3.
 */
public class StatDispatchHandlerTest {
    @InjectMocks
    StatDispatchHandler statDispatchHandler;
    @Mock
    private AgentStatHandlerV2 agentStatHandler;
    @Mock
    private AgentEventHandler agentEventHandler;
    @Mock
    private BusinessLogHandler businessLogHandler;
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getSimpleHandler() throws Exception {
        TAgentStat tAgentStat = new TAgentStat();
        List<SimpleHandler> agentStatListSimpleHandler = statDispatchHandler.getSimpleHandler(tAgentStat);
        Assert.assertEquals("size",agentStatListSimpleHandler.size(),2);

        TAgentStatBatch tAgentStatBatch =  new TAgentStatBatch();
        List<SimpleHandler> agentStatBatchListSimpleHandler = statDispatchHandler.getSimpleHandler(tAgentStatBatch);
        Assert.assertEquals("size",agentStatBatchListSimpleHandler.size(),2);

        TBusinessLog tBusinessLog = new TBusinessLog();
        List<SimpleHandler> businessLogSimpleHandler = statDispatchHandler.getSimpleHandler(tBusinessLog);
        Assert.assertEquals("size",businessLogSimpleHandler.size(),1);

        TBusinessLogBatch tBusinessLogBatch =  new TBusinessLogBatch();
        List<SimpleHandler> businessLogBatchListSimpleHandler = statDispatchHandler.getSimpleHandler(tBusinessLog);
        Assert.assertEquals("size",businessLogBatchListSimpleHandler.size(),1);
    }

}