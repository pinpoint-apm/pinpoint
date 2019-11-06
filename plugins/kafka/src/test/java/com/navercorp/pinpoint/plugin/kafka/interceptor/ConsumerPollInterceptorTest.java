package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.RemoteAddressFieldAccessor;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Iterator;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerPollInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private RemoteAddressFieldAccessor addressFieldAccessor;

    @Mock
    private ConsumerRecords consumerRecords;

    @Mock
    private Iterator iterator;


    @Test
    public void before() {
        ConsumerPollInterceptor interceptor = new ConsumerPollInterceptor(traceContext, descriptor);
        Object target = new Object();
        Object[] args = new Object[]{};
        interceptor.before(target, args);
    }

    @Test
    public void after() {

        doReturn("localhost:9092").when(addressFieldAccessor)._$PINPOINT$_getRemoteAddress();
        doReturn(iterator).when(consumerRecords).iterator();
        doReturn(false).when(iterator).hasNext();

        ConsumerPollInterceptor interceptor = new ConsumerPollInterceptor(traceContext, descriptor);
        interceptor.after(addressFieldAccessor, new Object[]{}, consumerRecords, null);

        verify(addressFieldAccessor)._$PINPOINT$_getRemoteAddress();

    }
}