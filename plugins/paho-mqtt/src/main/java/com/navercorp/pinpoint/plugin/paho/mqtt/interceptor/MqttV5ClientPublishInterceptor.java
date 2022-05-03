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

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.paho.mqtt.PahoMqttPluginConfig;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Younsung Hwang
 */
public class MqttV5ClientPublishInterceptor extends MqttClientPublishInterceptor {


    public MqttV5ClientPublishInterceptor(TraceContext traceContext, MethodDescriptor descriptor, PahoMqttPluginConfig config) {
        super(traceContext, descriptor, config);
    }

    @Override
    protected void setCallerDataWhenSampled(Trace trace, SpanEventRecorder recorder, Object[] args, boolean canSampled) {
        MqttProperties mqttProperties = getMqttProperties(args);
        if (mqttProperties == null) {
            return;
        }
        List<UserProperty> mqttUserProperties = mqttProperties.getUserProperties(); // mqttUserProperties may be AbstractList
        List<UserProperty> userPropertiesWithHeader = new ArrayList<>(mqttUserProperties);

        cleanPinpointHeader(userPropertiesWithHeader);
        if (canSampled) {
            final TraceId nextId = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(nextId.getSpanId());
            userPropertiesWithHeader.addAll(Arrays.asList(
                    new UserProperty(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId()),
                    new UserProperty(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId())),
                    new UserProperty(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId())),
                    new UserProperty(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags())),
                    new UserProperty(Header.HTTP_PARENT_APPLICATION_NAME.toString(), String.valueOf(traceContext.getApplicationName())),
                    new UserProperty(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()))
            ));
        } else {
            userPropertiesWithHeader.add(new UserProperty(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE));
        }
        mqttProperties.setUserProperties(userPropertiesWithHeader);
    }

    private MqttProperties getMqttProperties(Object[] args) {
        org.eclipse.paho.mqttv5.common.MqttMessage mqttMessage
                = ArrayArgumentUtils.getArgument(args, 1, org.eclipse.paho.mqttv5.common.MqttMessage.class);
        if (mqttMessage != null) {
            return mqttMessage.getProperties();
        }
        return null;
    }

    private void cleanPinpointHeader(List<UserProperty> userPropertiesWithHeader) {
        for (UserProperty userProperty : userPropertiesWithHeader) {
            String key = userProperty.getKey();
            if (Header.startWithPinpointHeader(key)) {
                userPropertiesWithHeader.remove(userProperty);
            }
        }
    }
}
