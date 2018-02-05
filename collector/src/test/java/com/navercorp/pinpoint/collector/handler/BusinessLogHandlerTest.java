package com.navercorp.pinpoint.collector.handler;

import com.navercorp.pinpoint.collector.dao.BusinessLogDao;
import com.navercorp.pinpoint.collector.mapper.thrift.stat.BusinessLogBatchMapper;
import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogBo;
import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogV1Bo;
import com.navercorp.pinpoint.thrift.dto.TBusinessLogBatch;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;


/**
 * Created by suny on 2018/2/3.
 */
public class BusinessLogHandlerTest {
    @InjectMocks
    BusinessLogHandler businessLogHandler;
    @Mock
    BusinessLogBatchMapper businessLogBatchMapper;
    @Mock
    BusinessLogDao<BusinessLogV1Bo> businessLogDao;
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handleSimpleThrowException() throws Exception {
           businessLogHandler.handleSimple(null);
    }

    @Test
    public void handleSimpleNormal(){
        TBusinessLogBatch tBusinessLogBatch = new TBusinessLogBatch();
        BusinessLogBo businessLogBo =  new BusinessLogBo();
        when(businessLogBatchMapper.map(tBusinessLogBatch)).thenReturn(businessLogBo);
        businessLogHandler.handleSimple(tBusinessLogBatch);
        verify(businessLogDao).insert(businessLogBo.getAgentId(),businessLogBo.getBusinessLogs());
    }
}