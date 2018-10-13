package com.navercorp.pinpoint.plugin.hbase;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HbasePluginMethodNameFilterTest {

    @Mock
    private InstrumentMethod method;

    @Test
    public void accept1() {
        doReturn(1).when(method).getModifiers();
        doReturn("tableExists").when(method).getName();
        HbasePluginMethodNameFilter filter = new HbasePluginMethodNameFilter(HbasePluginMethodNameFilter.MethodNameType.ADMIN);
        assertTrue(filter.accept(method));
        verify(method).getModifiers();
        verify(method).getName();
    }

    @Test
    public void accept2() {
        doReturn(1).when(method).getModifiers();
        doReturn("exists").when(method).getName();
        HbasePluginMethodNameFilter filter = new HbasePluginMethodNameFilter(HbasePluginMethodNameFilter.MethodNameType.TABLE);
        assertTrue(filter.accept(method));
        verify(method).getModifiers();
        verify(method).getName();
    }
}