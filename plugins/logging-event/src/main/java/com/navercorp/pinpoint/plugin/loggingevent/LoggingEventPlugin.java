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

package com.navercorp.pinpoint.plugin.loggingevent;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;
import java.util.List;
import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;


public class LoggingEventPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        LoggingEventConfig loggingEventConfig = new LoggingEventConfig(context.getConfig());
        if(!loggingEventConfig.isLoggingEventEnable()) {
            return;
        }
        addLog4j();
        addLogback();
    }

    private  void addLogback() {
        transformTemplate.transform("ch.qos.logback.classic.Logger", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                List<InstrumentMethod> listMethod = target.getDeclaredMethods();
                for(InstrumentMethod methodInfo : listMethod){
                    String name = methodInfo.getName().toLowerCase();
                    if(name.startsWith("trace") || name.startsWith("info") || name.startsWith("error") || name.startsWith("warn") || name.startsWith("debug") ){
                        methodInfo.addInterceptor("com.navercorp.pinpoint.plugin.loggingevent.interceptor.AppenderInterceptor");
                    }
                }
                logger.info("ch.qos.logback.classic.Logger is doing transform...");
                return target.toBytecode();
            }
        });
    }

    private void addLog4j() {
        transformTemplate.transform("org.apache.log4j.Category", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                List<InstrumentMethod> listMethod = target.getDeclaredMethods();
                for(InstrumentMethod methodInfo : listMethod){
                    String name = methodInfo.getName().toLowerCase();
                    if(name.startsWith("trace") || name.startsWith("info") || name.startsWith("error") || name.startsWith("warn") || name.startsWith("debug") ){
                        methodInfo.addInterceptor("com.navercorp.pinpoint.plugin.loggingevent.interceptor.AppenderInterceptor");
                    }
                }
                logger.info("org.apache.log4j.Category is doing transform...");
                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
