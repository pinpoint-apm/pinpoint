package com.navercorp.pinpoint.plugin.hbase.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.hbase.HbasePluginConstants;
import com.navercorp.pinpoint.plugin.hbase.HbaseVersion;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ClusterConnection;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Table;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
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
        int hbaseVersion = getHbaseVersion();
        HbaseTableMethodInterceptor interceptor = new HbaseTableMethodInterceptor(traceContext, descriptor, true, false, hbaseVersion, false);
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
        HbaseTableMethodInterceptor interceptor = new HbaseTableMethodInterceptor(traceContext, descriptor, true, true, hbaseVersion, false);
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
        HbaseTableMethodInterceptor interceptor = new HbaseTableMethodInterceptor(traceContext, descriptor, true, true, hbaseVersion, false);
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
        HbaseTableMethodInterceptor interceptor = new HbaseTableMethodInterceptor(traceContext, descriptor, true, true, hbaseVersion, false);

        Method method = interceptor.getClass().getDeclaredMethod("getTableName", Object.class);
        method.setAccessible(true);

        String hTableString = (String) method.invoke(interceptor, mockHTable);
        Assertions.assertEquals("HTable", hTableString);

        String unknownString = (String) method.invoke(interceptor, "1234");
        Assertions.assertEquals("Unknown", unknownString);
    }

    @Test
    public void doTestHBaseCalcSize() throws Exception {
        doReturn(new Configuration()).when(connection).getConfiguration();

        Table target = new HTable(TableName.valueOf("test"), connection);

        int hbaseVersion = getHbaseVersion();
        HbaseTableMethodInterceptor interceptor = new HbaseTableMethodInterceptor(traceContext, descriptor, true, true, hbaseVersion, true);

        interceptor.doInAfterTrace(recorder, target, null, null, null);
        verify(recorder).recordAttribute(HbasePluginConstants.HBASE_OP_DATA_SIZE, 0);
        verify(recorder).recordApi(descriptor);
        verify(recorder).recordException(null);
    }
}