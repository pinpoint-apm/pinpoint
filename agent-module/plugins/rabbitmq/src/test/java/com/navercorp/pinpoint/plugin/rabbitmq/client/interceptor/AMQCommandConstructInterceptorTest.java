/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.rabbitmq.client.RabbitMQClientConstants;
import com.navercorp.pinpoint.plugin.rabbitmq.client.field.setter.HeadersFieldSetter;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AMQCommandConstructInterceptorTest {

    private AMQCommandConstructInterceptor interceptor;
    private Method publish;
    private TestProperties properties;

    @BeforeEach
    public void setUp() {
        final Trace trace = mock(Trace.class);
        when(trace.canSampled()).thenReturn(false);
        final TraceContext traceContext = mock(TraceContext.class);
        when(traceContext.currentRawTraceObject()).thenReturn(trace);

        this.interceptor = new AMQCommandConstructInterceptor(traceContext, mock(InterceptorScope.class));
        this.publish = mock(Method.class);
        when(publish.protocolMethodName()).thenReturn("basic.publish");
        this.properties = new TestProperties();
    }

    @Test
    public void injectsHeaders_byteArrayBody() {
        // ~5.30.x (Method, AMQContentHeader, byte[])
        interceptor.before(new Object(), new Object[]{publish, properties, new byte[0]});

        assertThat(properties.injected).containsEntry(RabbitMQClientConstants.META_SAMPLED, "1");
    }

    @Test
    public void injectsHeaders_byteBufferBodyWithWriteListener() {
        // 5.31.0+ (Method, AMQContentHeader, ByteBuffer, WriteListener)
        interceptor.before(new Object(), new Object[]{publish, properties, ByteBuffer.allocate(0), null});

        assertThat(properties.injected).containsEntry(RabbitMQClientConstants.META_SAMPLED, "1");
    }

    @Test
    public void injectsHeaders_byteBufferBodyWithWriteListenerAndRunnable() {
        // 5.38.0+ (Method, AMQContentHeader, ByteBuffer, WriteListener, Runnable)
        interceptor.before(new Object(), new Object[]{publish, properties, ByteBuffer.allocate(0), null, null});

        assertThat(properties.injected).containsEntry(RabbitMQClientConstants.META_SAMPLED, "1");
    }

    @Test
    public void skips_whenBodyArgumentIsMissing() {
        interceptor.before(new Object(), new Object[]{publish, properties});

        assertThat(properties.injected).isNull();
    }

    @Test
    public void skips_whenNotBasicPublish() {
        final Method consume = mock(Method.class);
        when(consume.protocolMethodName()).thenReturn("basic.consume");

        interceptor.before(new Object(), new Object[]{consume, properties, ByteBuffer.allocate(0), null, null});

        assertThat(properties.injected).isNull();
    }

    private static class TestProperties extends AMQP.BasicProperties implements HeadersFieldSetter {
        private Map<String, Object> injected;

        @Override
        public void _$PINPOINT$_setHeaders(Map<String, Object> headers) {
            this.injected = headers;
        }
    }
}
