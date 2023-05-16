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
import com.navercorp.pinpoint.plugin.paho.mqtt.PahoMqttPluginConfig;

/**
 * @author Younsung Hwang
 */
public class MqttV3ClientPublishInterceptor extends MqttClientPublishInterceptor {

    public MqttV3ClientPublishInterceptor(TraceContext traceContext, MethodDescriptor descriptor, PahoMqttPluginConfig config) {
        super(traceContext, descriptor, config);
    }

    @Override
    protected void setCallerDataWhenSampled(Trace trace, SpanEventRecorder recorder, Object[] args, boolean canSampled) {
        // MQTT version 3 does not support custom header or properties.
        // So 'Caller' can't send data to 'Callee'.
    }


}
