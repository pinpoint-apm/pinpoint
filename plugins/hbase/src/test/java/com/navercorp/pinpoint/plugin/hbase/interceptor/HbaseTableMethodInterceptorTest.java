package com.navercorp.pinpoint.plugin.hbase.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.hbase.HbasePluginConstants;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ClusterConnection;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Table;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HbaseTableMethodInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private SpanEventRecorder recorder;

    @Mock
    private ClusterConnection connection;

    @Test
    public void doInBeforeTrace() {

        Object target = new Object();
        Object[] args = new Object[]{};

        HbaseTableMethodInterceptor interceptor = new HbaseTableMethodInterceptor(traceContext, descriptor,true, false);
        interceptor.doInBeforeTrace(recorder, target, args);
        verify(recorder).recordServiceType(HbasePluginConstants.HBASE_CLIENT_TABLE);
    }

    @Test
    public void doInAfterTrace() {

        Object target = new Object();
        Object[] args = new Object[]{Collections.singletonList("test")};

        HbaseTableMethodInterceptor interceptor = new HbaseTableMethodInterceptor(traceContext, descriptor, true ,true);
        interceptor.doInAfterTrace(recorder, target, args, null, null);
        verify(recorder).recordAttribute(HbasePluginConstants.HBASE_CLIENT_PARAMS, "size: 1");
        verify(recorder).recordApi(descriptor);
        verify(recorder).recordException(null);
    }

    @Test
    public void doTestHbaseTableName() throws Exception{

        doReturn(new Configuration()).when(connection).getConfiguration();

        Table target = new HTable(TableName.valueOf("test"), connection);

        Object[] args = new Object[]{Collections.singletonList("test")};

        HbaseTableMethodInterceptor interceptor = new HbaseTableMethodInterceptor(traceContext, descriptor, true, true);
        interceptor.doInAfterTrace(recorder, target, args, null, null);
        verify(recorder).recordAttribute(HbasePluginConstants.HBASE_TABLE_NAME, "test");
        verify(recorder).recordApi(descriptor);
        verify(recorder).recordException(null);
    }
}