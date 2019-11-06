package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.plugin.kafka.field.accessor.RemoteAddressFieldAccessor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerConstructorInterceptorTest {

    @Mock
    private RemoteAddressFieldAccessor addressFieldAccessor;

    @Mock
    private ConsumerConfig consumerConfig;

    @Test
    public void before() {

        ConsumerConstructorInterceptor interceptor = new ConsumerConstructorInterceptor();
        Object target = new Object();
        Object[] args = new Object[]{};
        interceptor.before(target, args);
    }

    @Test
    public void after() {

        doReturn(Collections.singletonList("localhost:9092")).when(consumerConfig).getList("bootstrap.servers");

        ConsumerConstructorInterceptor interceptor = new ConsumerConstructorInterceptor();
        Object[] args = new Object[]{consumerConfig};
        interceptor.after(addressFieldAccessor, args, null, null);

        verify(addressFieldAccessor)._$PINPOINT$_setRemoteAddress("localhost:9092");
    }
}