package com.navercorp.pinpoint.plugin.hbase.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.hbase.HbaseVersion;
import com.navercorp.pinpoint.plugin.hbase.HbasePluginConstants;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Table;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HbaseTableMethodInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private SpanEventRecorder recorder;

    @Test
    public void doInBeforeTrace() {

        Object target = new Object();
        Object[] args = new Object[]{};
        int hbaseVersion = getHbaseVersion();
        HbaseTableMethodInterceptor interceptor = new HbaseTableMethodInterceptor(traceContext, descriptor, true, false, hbaseVersion);
        interceptor.doInBeforeTrace(recorder, target, args);
        verify(recorder).recordServiceType(HbasePluginConstants.HBASE_CLIENT_TABLE);
    }

    private int getHbaseVersion() {
        return HbaseVersion.getVersion(this.getClass().getClassLoader());
    }

    @Test
    public void doInAfterTrace() {

        Object target = new Object();
        Object[] args = new Object[]{Collections.singletonList("test")};

        int hbaseVersion = getHbaseVersion();
        HbaseTableMethodInterceptor interceptor = new HbaseTableMethodInterceptor(traceContext, descriptor, true, true, hbaseVersion);
        interceptor.doInAfterTrace(recorder, target, args, null, null);
        verify(recorder).recordAttribute(HbasePluginConstants.HBASE_CLIENT_PARAMS, "size: 1");
        verify(recorder).recordApi(descriptor);
        verify(recorder).recordException(null);
    }

    @Test
    public void doTestHbaseTableName() {

        Table target = Mockito.mock(HTable.class);
        when(target.getName()).thenReturn(TableName.valueOf("test"));

        Object[] args = new Object[]{Collections.singletonList("test")};

        int hbaseVersion = getHbaseVersion();
        HbaseTableMethodInterceptor interceptor = new HbaseTableMethodInterceptor(traceContext, descriptor, true, true, hbaseVersion);
        interceptor.doInAfterTrace(recorder, target, args, null, null);
        verify(recorder).recordAttribute(HbasePluginConstants.HBASE_TABLE_NAME, "test");
        verify(recorder).recordApi(descriptor);
        verify(recorder).recordException(null);
    }

    @Test
    public void getTableName() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        HTable mockHTable = Mockito.mock(HTable.class);
        when(mockHTable.getName()).thenReturn(TableName.valueOf("HTable"));

        int hbaseVersion = getHbaseVersion();
        HbaseTableMethodInterceptor interceptor = new HbaseTableMethodInterceptor(traceContext, descriptor, true, true, hbaseVersion);

        Method method = interceptor.getClass().getDeclaredMethod("getTableName", Object.class);
        method.setAccessible(true);

        String hTableString = (String) method.invoke(interceptor, mockHTable);
        Assert.assertEquals("HTable", hTableString);

        String unknownString = (String) method.invoke(interceptor, "1234");
        Assert.assertEquals("Unknown", unknownString);
    }
}