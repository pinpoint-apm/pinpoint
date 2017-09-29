/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.plugin.activemq.client;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKeyProperty;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.QUEUE;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

/**
 * @author HyunGil Jeong
 */
public final class ActiveMQClientConstants {

    private ActiveMQClientConstants() {

    }

    public static final ServiceType ACTIVEMQ_CLIENT = ServiceTypeFactory.of(8310, "ACTIVEMQ_CLIENT", QUEUE, RECORD_STATISTICS);
    public static final ServiceType ACTIVEMQ_CLIENT_INTERNAL = ServiceTypeFactory.of(8311, "ACTIVEMQ_CLIENT_INTERNAL", "ACTIVE_MQ_CLIENT");

    public static final AnnotationKey ACTIVEMQ_BROKER_URL = AnnotationKeyFactory.of(101, "activemq.broker.address", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
    public static final AnnotationKey ACTIVEMQ_MESSAGE = AnnotationKeyFactory.of(102, "activemq.message", AnnotationKeyProperty.VIEW_IN_RECORD_SET);

    public static final String UNKNOWN_ADDRESS = "Unknown";

    public static final String ACTIVEMQ_CLIENT_SCOPE = "ActiveMQClientScope";

    private static final String PLUGIN_BASE = "com.navercorp.pinpoint.plugin.activemq.client";
    private static final String INTERCEPTOR_BASE = PLUGIN_BASE + ".interceptor";

    public static final String ACTIVEMQ_TCP_TRANSPORT_FQCN = "org.apache.activemq.transport.tcp.TcpTransport";
    public static final String ACTIVEMQ_FAILOVER_TRANSPORT_FQCN = "org.apache.activemq.transport.failover.FailoverTransport";

    public static final String ACTIVEMQ_CONNECTION_FQCN = "org.apache.activemq.ActiveMQConnection";

    public static final String ACTIVEMQ_SESSION_FQCN = "org.apache.activemq.ActiveMQSession";

    public static final String ACTIVEMQ_MESSAGE_DISPATCH_CHANNEL_FQCN = "org.apache.activemq.MessageDispatchChannel";
    public static final String ACTIVEMQ_MESSAGE_DISPATCH_CHANNEL_FIFO_FQCN = "org.apache.activemq.FifoMessageDispatchChannel";
    public static final String ACTIVEMQ_MESSAGE_DISPATCH_CHANNEL_SIMPLE_PRIORITY_FQCN = "org.apache.activemq.SimplePriorityMessageDispatchChannel";
    public static final String ACTIVEMQ_MESSAGE_DISPATCH_CHANNEL_ENQUEUE_INTERCEPTOR_FQCN = INTERCEPTOR_BASE + ".MessageDispatchChannelEnqueueInterceptor";
    public static final String ACTIVEMQ_MESSAGE_DISPATCH_CHANNEL_DEQUEUE_INTERCEPTOR_FQCN = INTERCEPTOR_BASE + ".MessageDispatchChannelDequeueInterceptor";

    public static final String ACTIVEMQ_MESSAGE_PRODUCER_FQCN = "org.apache.activemq.ActiveMQMessageProducer";
    public static final String ACTIVEMQ_MESSAGE_PRODUCER_SEND_INTERCEPTOR_FQCN = INTERCEPTOR_BASE + ".ActiveMQMessageProducerSendInterceptor";

    public static final String ACTIVEMQ_MESSAGE_CONSUMER_FQCN = "org.apache.activemq.ActiveMQMessageConsumer";
    public static final String ACTIVEMQ_MESSAGE_CONSUMER_DISPATCH_INTERCEPTOR_FQCN = INTERCEPTOR_BASE + ".ActiveMQMessageConsumerDispatchInterceptor";
    public static final String ACTIVEMQ_MESSAGE_CONSUMER_RECEIVE_INTERCEPTOR_FQCN = INTERCEPTOR_BASE + ".ActiveMQMessageConsumerReceiveInterceptor";

    // field names
    public static final String FIELD_TCP_TRANSPORT_SOCKET = "socket";
    public static final String FIELD_URI_TRANSPORT_SOCKET = "connectedTransportURI";
    public static final String FIELD_ACTIVEMQ_CONNECTION_TRANSPORT = "transport";
    public static final String FIELD_ACTIVEMQ_MESSAGE_PRODUCER_SESSION = "session";
    public static final String FIELD_ACTIVEMQ_MESSAGE_CONSUMER_SESSION = "session";

    private static final String FIELD_BASE = PLUGIN_BASE + ".field";
    private static final String FIELD_GETTER_BASE = FIELD_BASE + ".getter";

    // field getter FQCN
    public static final String FIELD_GETTER_SOCKET = FIELD_GETTER_BASE + ".SocketGetter";
    public static final String FIELD_GETTER_URI = FIELD_GETTER_BASE + ".URIGetter";
    public static final String FIELD_GETTER_TRANSPORT = FIELD_GETTER_BASE + ".TransportGetter";
    public static final String FIELD_GETTER_ACTIVEMQ_SESSION = FIELD_GETTER_BASE + ".ActiveMQSessionGetter";
}
