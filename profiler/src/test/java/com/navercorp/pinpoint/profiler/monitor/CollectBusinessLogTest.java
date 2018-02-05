package com.navercorp.pinpoint.profiler.monitor;

import com.navercorp.pinpoint.profiler.monitor.collector.BusinessLogMetaCollector;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.thrift.dto.TBusinessLog;
import com.navercorp.pinpoint.thrift.dto.TBusinessLogBatch;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by suny on 2018/2/5.
 */
public class CollectBusinessLogTest {
    @Mock
    BusinessLogMetaCollector businessLogMetaCollector;
    @Mock
    DataSender dataSender;
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void run() throws Exception {
            when(businessLogMetaCollector.collect()).thenReturn(new TBusinessLog());
            CollectBusinessLog job = new CollectBusinessLog(dataSender, "agent", 0, businessLogMetaCollector, 1);
            job.run();
            verify(dataSender).send(any(TBusinessLogBatch.class));


    }

}