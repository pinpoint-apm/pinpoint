package com.navercorp.pinpoint.profiler.monitor.collector;

import com.navercorp.pinpoint.profiler.monitor.collector.businesslog.BusinessLogVXMetaCollector;
import com.navercorp.pinpoint.thrift.dto.TBusinessLog;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

/**
 * Created by suny on 2018/2/5.
 */
public class BusinessLogTotalCollectorTest {
    @Mock
    BusinessLogVXMetaCollector businessLogVXMetaCollector;
    @InjectMocks
    BusinessLogTotalCollector businessLogTotalCollector;
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void collect() throws Exception {
        TBusinessLog tBusinessLog = businessLogTotalCollector.collect();
        Assert.assertNotNull(tBusinessLog);
        verify(businessLogVXMetaCollector).collect();
    }

    @Test
    public void saveLogMark() throws Exception {
        businessLogTotalCollector.saveLogMark();
        verify(businessLogVXMetaCollector).saveLogMark();
    }

}