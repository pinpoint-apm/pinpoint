package com.navercorp.pinpoint.common.hbase.async;

import org.apache.hadoop.hbase.client.AsyncBufferedMutatorBuilder;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class DefaultAsyncBufferedMutatorCustomizerTest {

    @Test
    void customize_default() {
        AsyncBufferedMutatorBuilder builder = mock(AsyncBufferedMutatorBuilder.class);

        DefaultAsyncBufferedMutatorCustomizer customizer = new DefaultAsyncBufferedMutatorCustomizer();
        customizer.setWriteBufferSize(0);
        customizer.setWriteBufferPeriodicFlush(0);

        customizer.customize(builder);

        verify(builder, never()).setWriteBufferSize(0);
        verify(builder, never()).setWriteBufferPeriodicFlush(0, TimeUnit.MILLISECONDS);
    }

    @Test
    void customize() {
        AsyncBufferedMutatorBuilder builder = mock(AsyncBufferedMutatorBuilder.class);

        DefaultAsyncBufferedMutatorCustomizer customizer = new DefaultAsyncBufferedMutatorCustomizer();
        customizer.setWriteBufferSize(200);
        customizer.setWriteBufferPeriodicFlush(400);

        customizer.customize(builder);

        verify(builder).setWriteBufferSize(200);
        verify(builder).setWriteBufferPeriodicFlush(400, TimeUnit.MILLISECONDS);
    }

}