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

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.paho.mqtt.PahoMqttPluginConfig;
import com.navercorp.pinpoint.plugin.paho.mqtt.accessor.BrokerUriFieldAccessor;

import static com.navercorp.pinpoint.plugin.paho.mqtt.PahoMqttConstants.MQTT_BROKER_URI_ANNOTATION_KEY;
import static com.navercorp.pinpoint.plugin.paho.mqtt.PahoMqttConstants.MQTT_MESSAGE_PAYLOAD_ANNOTATION_KEY;
import static com.navercorp.pinpoint.plugin.paho.mqtt.PahoMqttConstants.MQTT_QOS_ANNOTATION_KEY;
import static com.navercorp.pinpoint.plugin.paho.mqtt.PahoMqttConstants.MQTT_TOPIC_ANNOTATION_KEY;
import static com.navercorp.pinpoint.plugin.paho.mqtt.PahoMqttConstants.PAHO_MQTT_CLIENT;
import static com.navercorp.pinpoint.plugin.paho.mqtt.PahoMqttConstants.UNKNOWN;

/**
 * @author Younsung Hwang
 */
public abstract class MqttClientPublishInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    protected PahoMqttPluginConfig config;
    protected final TraceContext traceContext;
    protected final MethodDescriptor descriptor;

    public MqttClientPublishInterceptor(TraceContext traceContext, MethodDescriptor descriptor, PahoMqttPluginConfig config) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.config = config;
    }

    @Override
    public void before(Object target, Object[] args) {

        if (logger.isDebugEnabled()) {
            logger.beforeInterceptor(target, args);
        }

        if (ArrayUtils.isEmpty(args)) {
            return;
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        SpanEventRecorder recorder = null;
        if (trace.canSampled()) {
            recorder = trace.traceBlockBegin();
            recorder.recordServiceType(PAHO_MQTT_CLIENT);

            TraceId nextId = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(nextId.getSpanId());
        }
        setCallerDataWhenSampled(trace, recorder, args, trace.canSampled());
    }

    protected abstract void setCallerDataWhenSampled(Trace trace, SpanEventRecorder recorder, Object[] args, boolean canSampled);

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        if (ArrayUtils.isEmpty(args)) {
            return;
        }
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null || !trace.canSampled()) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);

            String brokerUri = getBrokerUri(target);
            recorder.recordEndPoint(brokerUri);
            recorder.recordDestinationId(brokerUri);

            recorder.recordAttribute(MQTT_BROKER_URI_ANNOTATION_KEY, brokerUri);
            recorder.recordAttribute(MQTT_TOPIC_ANNOTATION_KEY, getTopic(args));
            recorder.recordAttribute(MQTT_MESSAGE_PAYLOAD_ANNOTATION_KEY, getPayload(args));
            recorder.recordAttribute(MQTT_QOS_ANNOTATION_KEY, getQos(args));

            if (throwable != null) {
                recorder.recordException(throwable);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private String getTopic(Object args[]) {
        if (args[0] instanceof String) {
            return (String) args[0];
        }
        return null;
    }

    private String getPayload(Object args[]) {
        if(this.config.isEnableTracePahoMqttClientV3() && args[1] instanceof org.eclipse.paho.client.mqttv3.MqttMessage){
            org.eclipse.paho.client.mqttv3.MqttMessage mqttMessage = (org.eclipse.paho.client.mqttv3.MqttMessage)args[1];
            return BytesUtils.toString(mqttMessage.getPayload());
        }
        if(this.config.isEnableTracePahoMqttClientV5() && args[1] instanceof org.eclipse.paho.mqttv5.common.MqttMessage){
            org.eclipse.paho.mqttv5.common.MqttMessage mqttMessage =  (org.eclipse.paho.mqttv5.common.MqttMessage)args[1];
            return BytesUtils.toString(mqttMessage.getPayload());
        }
        return "";
    }

    private int getQos(Object args[]) {
        if (this.config.isEnableTracePahoMqttClientV3() && args[1] instanceof org.eclipse.paho.client.mqttv3.MqttMessage) {
            org.eclipse.paho.client.mqttv3.MqttMessage mqttMessage = (org.eclipse.paho.client.mqttv3.MqttMessage) args[1];
            return mqttMessage.getQos();
        }
        if (this.config.isEnableTracePahoMqttClientV5() && args[1] instanceof org.eclipse.paho.mqttv5.common.MqttMessage) {
            org.eclipse.paho.mqttv5.common.MqttMessage mqttMessage = (org.eclipse.paho.mqttv5.common.MqttMessage) args[1];
            return mqttMessage.getQos();
        }
        return 0;
    }

    private String getBrokerUri(Object target) {

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
