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

package com.navercorp.pinpoint.plugin.paho.mqtt;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.paho.mqtt.accessor.BrokerUriFieldAccessor;
import com.navercorp.pinpoint.plugin.paho.mqtt.accessor.MqttV3ClientCommsGetter;
import com.navercorp.pinpoint.plugin.paho.mqtt.accessor.MqttV5ClientCommsGetter;
import com.navercorp.pinpoint.plugin.paho.mqtt.accessor.SocketGetter;
import com.navercorp.pinpoint.plugin.paho.mqtt.interceptor.*;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Younsung Hwang
 */
public class PahoMqttPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    @Override
    public void setup(ProfilerPluginSetupContext context) {

        PahoMqttPluginConfig config = new PahoMqttPluginConfig(context.getConfig());

        if(!config.isEnableTracePahoMqttClient()){
            logger.debug("disable paho.mqtt plugin");
            return;
        }
        logger.info("Paho mqtt plugin config[{}]", config);

        setUpMqttV3(config);
        setUpMqttV5(config);

    }

    private void setUpMqttV3(PahoMqttPluginConfig config) {
        if(config.isEnableTracePahoMqttClientV3() && config.isEnableTracePahoMqttClientPublisher()){
            transformTemplate.transform("org.eclipse.paho.client.mqttv3.MqttAsyncClient", MqttV3AsyncClientTransform.class);
        }

        if(config.isEnableTracePahoMqttClientV3() && config.isEnableTracePahoMqttClientSubscriber()){
            transformTemplate.transform("org.eclipse.paho.client.mqttv3.internal.CommsCallback", MqttV3CallbackTransform.class);
            transformTemplate.transform("org.eclipse.paho.client.mqttv3.internal.TCPNetworkModule", NetworkModuleTransForm.class);
            transformTemplate.transform("org.eclipse.paho.client.mqttv3.internal.SSLNetworkModule", NetworkModuleTransForm.class);
        }
    }

    private void setUpMqttV5(PahoMqttPluginConfig config) {
        if(config.isEnableTracePahoMqttClientV5() && config.isEnableTracePahoMqttClientPublisher()){
            transformTemplate.transform("org.eclipse.paho.mqttv5.client.MqttAsyncClient", MqttV5AsyncClientTransform.class);
        }

        if(config.isEnableTracePahoMqttClientV5() && config.isEnableTracePahoMqttClientSubscriber()){
            transformTemplate.transform("org.eclipse.paho.mqttv5.client.internal.CommsCallback", MqttV5CallbackTransform.class);
            transformTemplate.transform("org.eclipse.paho.mqttv5.client.internal.TCPNetworkModule", NetworkModuleTransForm.class);
            transformTemplate.transform("org.eclipse.paho.mqttv5.client.internal.SSLNetworkModule", NetworkModuleTransForm.class);
        }
    }

    public static class MqttV3AsyncClientTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            PahoMqttPluginConfig config = new PahoMqttPluginConfig(instrumentor.getProfilerConfig());
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            // v1.2.5 is supported
            InstrumentMethod constructor = target.getConstructor(
                    "java.lang.String",
                    "java.lang.String",
                    "org.eclipse.paho.client.mqttv3.MqttClientPersistence",
                    "org.eclipse.paho.client.mqttv3.MqttPingSender",
                    "java.util.concurrent.ScheduledExecutorService",
                    "org.eclipse.paho.client.mqttv3.internal.HighResolutionTimer"
            );
            if(constructor == null) {
                // v1.1.x, v1.0.x  is supported
                constructor = target.getConstructor(
                        "java.lang.String",
                        "java.lang.String",
                        "org.eclipse.paho.client.mqttv3.MqttClientPersistence",
                        "org.eclipse.paho.client.mqttv3.MqttPingSender"
                );
            }
            if(constructor != null) {
                constructor.addInterceptor(MqttClientConstructorInterceptor.class);
            }

            InstrumentMethod mqttV3PublishMethod = target.getDeclaredMethod("publish",
                    "java.lang.String",
                    "org.eclipse.paho.client.mqttv3.MqttMessage",
                    "java.lang.Object",
                    "org.eclipse.paho.client.mqttv3.IMqttActionListener"
            );
            mqttV3PublishMethod.addInterceptor(MqttV3ClientPublishInterceptor.class, va(config));

            target.addField(BrokerUriFieldAccessor.class);

            return target.toBytecode();
        }
    }

    public static class MqttV5AsyncClientTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            PahoMqttPluginConfig config = new PahoMqttPluginConfig(instrumentor.getProfilerConfig());
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            InstrumentMethod constructor = target.getConstructor(
                    "java.lang.String",
                    "java.lang.String",
                    "org.eclipse.paho.mqttv5.client.MqttClientPersistence",
                    "org.eclipse.paho.mqttv5.client.MqttPingSender",
                    "java.util.concurrent.ScheduledExecutorService"
            );
            constructor.addInterceptor(MqttClientConstructorInterceptor.class);

            InstrumentMethod mqttV5PublishMethod = target.getDeclaredMethod("publish",
                    "java.lang.String",
                    "org.eclipse.paho.mqttv5.common.MqttMessage",
                    "java.lang.Object",
                    "org.eclipse.paho.mqttv5.client.MqttActionListener"
            );
            mqttV5PublishMethod.addInterceptor(MqttV5ClientPublishInterceptor.class, va(config));

            target.addField(BrokerUriFieldAccessor.class);

            return target.toBytecode();
        }
    }

    public static class MqttV3CallbackTransform implements TransformCallback{
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            InstrumentMethod constructor = target.getConstructor("org.eclipse.paho.client.mqttv3.internal.ClientComms");
            constructor.addInterceptor(CommsCallbackV3ConstructorInterceptor.class);

            InstrumentMethod messageArrivedMethod = target.getDeclaredMethod("messageArrived", "org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish");
            messageArrivedMethod.addInterceptor(MqttV3CallbackMessageArrivedInterceptor.class);

            target.addGetter(MqttV3ClientCommsGetter.class, "clientComms");
            target.addField(BrokerUriFieldAccessor.class);

            return target.toBytecode();
        }
    }

    public static class MqttV5CallbackTransform implements TransformCallback{
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            InstrumentMethod constructor = target.getConstructor("org.eclipse.paho.mqttv5.client.internal.ClientComms");
            constructor.addInterceptor(CommsCallbackV5ConstructorInterceptor.class);

            InstrumentMethod messageArrivedMethod = target.getDeclaredMethod("messageArrived", "org.eclipse.paho.mqttv5.common.packet.MqttPublish");
            messageArrivedMethod.addInterceptor(MqttV5CallbackMessageArrivedInterceptor.class);

            target.addGetter(MqttV5ClientCommsGetter.class, "clientComms");
            target.addField(BrokerUriFieldAccessor.class);

            return target.toBytecode();
        }
    }

    public static class NetworkModuleTransForm implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addGetter(SocketGetter.class, "socket");
            return target.toBytecode();
        }
    }
}
