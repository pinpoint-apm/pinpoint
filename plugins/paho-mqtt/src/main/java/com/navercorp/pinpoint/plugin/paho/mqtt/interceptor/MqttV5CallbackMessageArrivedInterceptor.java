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
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.plugin.paho.mqtt.accessor.MqttV5ClientCommsGetter;
import com.navercorp.pinpoint.plugin.paho.mqtt.accessor.SocketGetter;
import org.eclipse.paho.mqttv5.client.internal.ClientComms;
import org.eclipse.paho.mqttv5.client.internal.NetworkModule;
import org.eclipse.paho.mqttv5.common.packet.MqttPublish;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;

import java.net.Socket;
import java.util.List;

import static com.navercorp.pinpoint.plugin.paho.mqtt.PahoMqttConstants.MQTT_MESSAGE_PAYLOAD_ANNOTATION_KEY;
import static com.navercorp.pinpoint.plugin.paho.mqtt.PahoMqttConstants.UNKNOWN;

/**
 * @author Younsung Hwang
 */
public class MqttV5CallbackMessageArrivedInterceptor extends MqttCallbackMessageArrivedInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    public MqttV5CallbackMessageArrivedInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected Trace createTraceByVersion(Object target, Object[] args) {
        MqttPublish mqttPublish = getMqttPublish(args);
        if (mqttPublish == null) {
            return null;
        }

        List<UserProperty> userProperties = mqttPublish.getProperties().getUserProperties();
        if (!isPreviousSpanSampled(userProperties)) {
            final Trace trace = traceContext.disableSampling();
            if (isDebug) {
                logger.debug("remotecall sampling flag found. skip trace");
            }
            return trace;
        }

        TraceId traceId = createTraceIdFromProperties(userProperties);
        if (traceId != null) {
            return traceContext.continueTraceObject(traceId);
        } else {
            return traceContext.newTraceObject();
        }
    }

    @Override
    protected void recordDataByVersion(Object target, SpanRecorder recorder, Object[] args) {

        if (args[0] instanceof MqttPublish) {

            MqttPublish mqttPublish = (MqttPublish) args[0];
            recorder.recordRpcName(buildRpcName(mqttPublish.getTopicName(), mqttPublish.getQoS()));
            recorder.recordAttribute(MQTT_MESSAGE_PAYLOAD_ANNOTATION_KEY, BytesUtils.toString(mqttPublish.getPayload()));

            List<UserProperty> userProperties = mqttPublish.getProperties().getUserProperties();
            recordParentApplication(recorder, userProperties);

            String endPoint = getEndPoint(target);
            recorder.recordEndPoint(endPoint);
        }

    }

    @Override
    protected boolean validateArgs(Object[] args) {

        if (args[0] instanceof MqttPublish) {
            return true;
        }
        return false;

    }

    private void recordParentApplication(SpanRecorder recorder, List<UserProperty> userProperties) {

        String parentApplicationName = null, parentApplicationType = null;

        for (UserProperty property : userProperties) {
            if (property.getKey().equals(Header.HTTP_PARENT_APPLICATION_NAME.toString())) {
                parentApplicationName = property.getValue();
            } else if (property.getKey().equals(Header.HTTP_PARENT_APPLICATION_TYPE.toString())) {
                parentApplicationType = property.getValue();
            }
        }

        recorder.recordParentApplication(parentApplicationName, NumberUtils.parseShort(parentApplicationType, ServiceType.UNDEFINED.getCode()));
    }

    private MqttPublish getMqttPublish(Object[] args) {
        if (args[0] instanceof MqttPublish) {
            return (MqttPublish) args[0];
        }
        return null;
    }

    private boolean isPreviousSpanSampled(List<UserProperty> userProperties) {
        UserProperty sampledProperty = null;
        for (UserProperty userProperty : userProperties) {
            if (userProperty.getKey().equals(Header.HTTP_SAMPLED.toString())) {
                sampledProperty = userProperty;
                break;
            }
        }
        if (sampledProperty == null) {
            return true;
        }
        return SamplingFlagUtils.isSamplingFlag(sampledProperty.getValue());
    }

    private TraceId createTraceIdFromProperties(List<UserProperty> userProperties) {

        String transactionId = null;
        String spanID = null;
        String parentSpanID = null;
        String flags = null;

        for (UserProperty property : userProperties) {
            if (property.getKey().equals(Header.HTTP_TRACE_ID.toString())) {
                transactionId = property.getValue();
            } else if (property.getKey().equals(Header.HTTP_PARENT_SPAN_ID.toString())) {
                parentSpanID = property.getValue();
            } else if (property.getKey().equals(Header.HTTP_SPAN_ID.toString())) {
                spanID = property.getValue();
            } else if (property.getKey().equals(Header.HTTP_FLAGS.toString())) {
                flags = property.getValue();
            }
        }

        if (transactionId == null || spanID == null || parentSpanID == null || flags == null) {
            return null;
        }

        return traceContext.createTraceId(transactionId, Long.parseLong(parentSpanID), Long.parseLong(spanID), Short.parseShort(flags));
    }

    private String getEndPoint(Object target) {

        ClientComms clientComms = ((MqttV5ClientCommsGetter) target)._$PINPOINT$_getMqttV5ClientComms();
        if (clientComms == null) {
            return UNKNOWN;
        }

        NetworkModule networkModule = clientComms.getNetworkModules()[clientComms.getNetworkModuleIndex()];
        if (networkModule == null) {
            return UNKNOWN;
        }

        Socket socket = ((SocketGetter) networkModule)._$PINPOINT$_getSocket();
        return HostAndPort.toHostAndPortString(socket.getLocalAddress().getHostAddress(), socket.getLocalPort());
    }
}
