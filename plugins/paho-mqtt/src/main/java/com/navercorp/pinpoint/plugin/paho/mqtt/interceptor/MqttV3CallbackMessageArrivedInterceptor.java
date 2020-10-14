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
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.plugin.paho.mqtt.accessor.MqttV3ClientCommsGetter;
import com.navercorp.pinpoint.plugin.paho.mqtt.accessor.SocketGetter;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.ClientComms;
import org.eclipse.paho.client.mqttv3.internal.NetworkModule;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;

import java.net.Socket;

import static com.navercorp.pinpoint.plugin.paho.mqtt.PahoMqttConstants.MQTT_MESSAGE_PAYLOAD_ANNOTATION_KEY;
import static com.navercorp.pinpoint.plugin.paho.mqtt.PahoMqttConstants.UNKNOWN;

/**
 * @author Younsung Hwang
 */
public class MqttV3CallbackMessageArrivedInterceptor extends MqttCallbackMessageArrivedInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    public MqttV3CallbackMessageArrivedInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected Trace createTraceByVersion(Object target, Object[] args) {
        // MQTT Version 3 not supoort custom properties, so can't match publish trace
        return traceContext.newTraceObject();
    }

    @Override
    protected boolean validateArgs(Object[] args) {

        if (args[0] instanceof MqttPublish) {
            return true;
        }
        return false;

    }

    @Override
    protected void recordDataByVersion(Object target, SpanRecorder recorder, Object[] args) {

        if (args[0] instanceof MqttPublish) {

            MqttPublish mqttPublish = (MqttPublish) args[0];

            recorder.recordRpcName(buildRpcName(mqttPublish.getTopicName(), mqttPublish.getMessage().getQos()));

            MqttMessage mqttMessage = mqttPublish.getMessage();
            String payload = BytesUtils.toString(mqttMessage.getPayload());
            recorder.recordAttribute(MQTT_MESSAGE_PAYLOAD_ANNOTATION_KEY, payload);

            String endPoint = getEndPoint(target);
            recorder.recordEndPoint(endPoint);
        }
    }

    private String getEndPoint(Object target) {

        ClientComms clientComms = ((MqttV3ClientCommsGetter) target)._$PINPOINT$_getMqttV3ClientComms();
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
