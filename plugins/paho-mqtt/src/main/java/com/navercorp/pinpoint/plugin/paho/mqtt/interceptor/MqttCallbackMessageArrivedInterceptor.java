/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.paho.mqtt.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanRecursiveAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.paho.mqtt.accessor.BrokerUriFieldAccessor;
import com.navercorp.pinpoint.plugin.paho.mqtt.descriptor.MqttSubscribeEntryMethodDescriptor;

import static com.navercorp.pinpoint.plugin.paho.mqtt.PahoMqttConstants.*;

/**
 * @author Younsung Hwang
 */
public abstract class MqttCallbackMessageArrivedInterceptor extends SpanRecursiveAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private static final String SCOPE_NAME = "##PAHO_MQTT_MESSAGE_ARRIVED_START_TRACE";

    protected static final MqttSubscribeEntryMethodDescriptor ENTRY_POINT_METHOD_DESCRIPTOR = new MqttSubscribeEntryMethodDescriptor();

    public MqttCallbackMessageArrivedInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor, SCOPE_NAME);
        traceContext.cacheApi(ENTRY_POINT_METHOD_DESCRIPTOR);
    }

    @Override
    protected Trace createTrace(Object target, Object[] args) {

        if(!validateArgs(args)){
            return null;
        }

        Trace trace = createTraceByVersion(target, args);
        if (trace.canSampled()) {
            final SpanRecorder recorder = trace.getSpanRecorder();
            recordCommonData(target, recorder);
            recordDataByVersion(target, recorder, args);
        } else {
            if (isDebug) {
                logger.debug("TraceID not exist. camSampled is false. skip trace.");
            }
        }
        return trace;
    }

    protected abstract boolean validateArgs(Object[] args);
    protected abstract Trace createTraceByVersion(Object target, Object[] args);
    protected abstract void recordDataByVersion(Object target, SpanRecorder recorder, Object[] args);

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(PAHO_MQTT_CLIENT_INTERNAL);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }

    private void recordCommonData(Object target, SpanRecorder recorder) {

        recorder.recordServiceType(PAHO_MQTT_CLIENT);
        recorder.recordApi(ENTRY_POINT_METHOD_DESCRIPTOR);

        String brokerUri = getBrokerUri(target);
        recorder.recordRemoteAddress(brokerUri);
        recorder.recordAcceptorHost(brokerUri);
    }

    protected String buildRpcName(String topic, int qos) {
        return new StringBuilder("mqtt://topic=").append(topic)
                .append("&qos=").append(qos)
                .toString();
    }

    protected String getBrokerUri(Object target) {

        String brokerUri = null;
        if (target instanceof BrokerUriFieldAccessor) {
            brokerUri = ((BrokerUriFieldAccessor) target)._$PINPOINT$_getBrokerUri();
        }

        if (StringUtils.isEmpty(brokerUri)) {
            return UNKNOWN;
        } else {
            return brokerUri;
        }
    }

}
