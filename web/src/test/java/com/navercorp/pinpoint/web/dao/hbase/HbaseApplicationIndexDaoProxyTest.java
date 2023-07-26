package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexPerTimeDao;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class HbaseApplicationIndexDaoProxyTest {

    @Mock
    private ApplicationIndexDao applicationIndexDao;

    @Mock
    private ApplicationIndexPerTimeDao applicationIndexPerTimeDao;

    private String applicationName = "applicationName";

    @Test
    public void selectAllApplicationNamesTest() {
        doReturn(Collections.emptyList()).when(applicationIndexDao).selectAllApplicationNames();
        HbaseApplicationIndexDaoProxy hbaseApplicationIndexDaoProxy = new HbaseApplicationIndexDaoProxy(applicationIndexDao, applicationIndexPerTimeDao);

        hbaseApplicationIndexDaoProxy.selectAllApplicationNames();
        verify(applicationIndexDao, times(1)).selectAllApplicationNames();
    }

    @Test
    public void selectApplicationNameTest() {
        doReturn(Collections.emptyList()).when(applicationIndexDao).selectApplicationName(applicationName);
        HbaseApplicationIndexDaoProxy hbaseApplicationIndexDaoProxy = new HbaseApplicationIndexDaoProxy(applicationIndexDao, applicationIndexPerTimeDao);

        hbaseApplicationIndexDaoProxy.selectApplicationName(applicationName);
        verify(applicationIndexDao, times(1)).selectApplicationName(applicationName);
    }

    @Test
    public void selectAgentIdsTest() {
        Range range = Range.between(0, 1);
        doReturn(Collections.emptyList()).when(applicationIndexDao).selectAgentIds(applicationName);
        doReturn(Collections.emptyList()).when(applicationIndexPerTimeDao).selectAgentIds(applicationName, range);
        HbaseApplicationIndexDaoProxy hbaseApplicationIndexDaoProxy = new HbaseApplicationIndexDaoProxy(applicationIndexDao, applicationIndexPerTimeDao);

        hbaseApplicationIndexDaoProxy.setUseIndexPerTime(false);
        hbaseApplicationIndexDaoProxy.selectAgentIds(applicationName);
        verify(applicationIndexDao, times(1)).selectAgentIds(applicationName);

        hbaseApplicationIndexDaoProxy.setUseIndexPerTime(true);
        hbaseApplicationIndexDaoProxy.selectAgentIds(applicationName, range);
        verify(applicationIndexPerTimeDao, times(1)).selectAgentIds(applicationName, range);
    }
}
