/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;

import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.DefaultAgentOption;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.common.service.DefaultAnnotationKeyRegistryService;
import com.navercorp.pinpoint.common.service.DefaultServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.thrift.dto.TAnnotation;

/**
 * @author emeroad
 * @author koo.taejin
 * @author hyungil.jeong
 */
public class MockApplicationContext extends DefaultApplicationContext {
    private InterceptorRegistryBinder interceptorRegistryBinder;

    public static MockApplicationContext of(String configPath) {
        ProfilerConfig profilerConfig = null;
        try {
            final URL resource = MockApplicationContext.class.getClassLoader().getResource(configPath);
            if (resource == null) {
                throw new FileNotFoundException("pinpoint.config not found. configPath:" + configPath);
            }
            profilerConfig = DefaultProfilerConfig.load(resource.getPath());
            ((DefaultProfilerConfig)profilerConfig).setApplicationServerType(ServiceType.TEST_STAND_ALONE.getName());
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        return of(profilerConfig);
    }
    
    public static MockApplicationContext of(ProfilerConfig config) {
        AgentOption agentOption = new DefaultAgentOption(new DummyInstrumentation(), "mockAgent", "mockApplicationName", config, new URL[0], null, new DefaultServiceTypeRegistryService(), new DefaultAnnotationKeyRegistryService());
        InterceptorRegistryBinder binder = new TestInterceptorRegistryBinder();
        binder.bind();

        return new MockApplicationContext(agentOption, binder);
    }


    public MockApplicationContext(AgentOption agentOption, InterceptorRegistryBinder binder) {
        super(agentOption, binder);
        this.interceptorRegistryBinder = binder;
    }

    @Override
    protected Module newApplicationContextModule(AgentOption agentOption, InterceptorRegistryBinder interceptorRegistryBinder) {
        Module applicationContextModule = super.newApplicationContextModule(agentOption, interceptorRegistryBinder);
        MockApplicationContextModule mockApplicationContextModule = new MockApplicationContextModule();

        return Modules.override(applicationContextModule).with(mockApplicationContextModule);
    }





    @Override
    public void close() {
        super.close();
        if (this.interceptorRegistryBinder != null) {
            interceptorRegistryBinder.unbind();
        }
    }




    public static String toString(Span span) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(span.getServiceType());
        builder.append(", [");
        appendAnnotations(builder, span.getAnnotations());
        builder.append("])");
        
        return builder.toString();
    }

    public static String toString(SpanEvent span) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(span.getServiceType());
        builder.append(", [");
        appendAnnotations(builder, span.getAnnotations());
        builder.append("])");
        
        return builder.toString();
    }

    private static void appendAnnotations(StringBuilder builder, List<TAnnotation> annotations) {
        boolean first = true;
        
        if (annotations != null) {
            for (TAnnotation a : annotations) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                
                builder.append(toString(a));
            }
        }
    }

    private static String toString(TAnnotation a) {
        return a.getKey() + "=" + a.getValue().getFieldValue();
    }
    
    public static String toString(short serviceCode, ExpectedAnnotation...annotations) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(serviceCode);
        builder.append(", ");
        builder.append(Arrays.deepToString(annotations));
        builder.append(')');
        
        return builder.toString();
    }
}
