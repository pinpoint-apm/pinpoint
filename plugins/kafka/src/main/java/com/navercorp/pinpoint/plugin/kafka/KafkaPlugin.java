/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kafka;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

import java.security.ProtectionDomain;


public class KafkaPlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final KafkaConfig config = new KafkaConfig(context.getConfig());
        if (!config.isEnable()) return;
        transformTemplate.transform("org.apache.kafka.clients.producer.KafkaProducer", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                InstrumentMethod method = target.getDeclaredMethod("send", "org.apache.kafka.clients.producer.ProducerRecord", "org.apache.kafka.clients.producer.Callback");
                method.addInterceptor("com.navercorp.pinpoint.plugin.kafka.interceptor.KafkaProducerSendInterceptor");
                return target.toBytecode();
            }
        });

        transformTemplate.transform("org.apache.kafka.clients.consumer.ConsumerRecord", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                if (config.isCreateContext()) {
                    InstrumentMethod method = target.getConstructor("java.lang.String", "int", "long", "long", "org.apache.kafka.common.record.TimestampType",
                            "java.lang.Long", "int", "int", "java.lang.Object", "java.lang.Object", "org.apache.kafka.common.header.Headers");
                    method.addInterceptor("com.navercorp.pinpoint.plugin.kafka.interceptor.ConsumerRecordConstructorInterceptor");
                }
                if (config.isIncludeHeader()) {
                    target.weave("com.navercorp.pinpoint.plugin.kafka.aspect.ConsumerRecordAspect");
                }
                return target.toBytecode();
            }
        });

        transformTemplate.transform("org.apache.kafka.clients.consumer.KafkaConsumer", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                InstrumentMethod method = target.getConstructor("org.apache.kafka.clients.consumer.ConsumerConfig",
                        "org.apache.kafka.common.serialization.Deserializer", "org.apache.kafka.common.serialization.Deserializer");
                method.addInterceptor("com.navercorp.pinpoint.plugin.kafka.interceptor.KafkaConsumerConstructorInterceptor");
                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
