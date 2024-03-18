package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.plugin.kafka.field.accessor.RemoteAddressFieldAccessor;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Iterator;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ConsumerPollInterceptorTest {

    @Mock
    private RemoteAddressFieldAccessor addressFieldAccessor;

    @Mock
    private ConsumerRecords consumerRecords;

    @Mock
    private Iterator iterator;


    @Test
    public void before() {
        ConsumerPollInterceptor interceptor = new ConsumerPollInterceptor();
        Object target = new Object();
        Object[] args = new Object[]{};
        interceptor.before(target, args);
    }

    @Test
    public void after() {

        doReturn("localhost:9092").when(addressFieldAccessor)._$PINPOINT$_getRemoteAddress();
        doReturn(iterator).when(consumerRecords).iterator();
        doReturn(false).when(iterator).hasNext();

        ConsumerPollInterceptor interceptor = new ConsumerPollInterceptor();
        interceptor.after(addressFieldAccessor, new Object[]{}, consumerRecords, null);

        verify(addressFieldAccessor)._$PINPOINT$_getRemoteAddress();

    }
}