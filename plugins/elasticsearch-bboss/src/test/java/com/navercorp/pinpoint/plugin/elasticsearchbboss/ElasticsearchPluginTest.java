/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.elasticsearchbboss;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import org.junit.Assert;
import org.junit.Test;

import java.security.ProtectionDomain;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchPluginTest {
    private ElasticsearchPlugin elasticsearchPlugin = new ElasticsearchPlugin();

    @Test
    public void testSetup() {
        ProfilerPluginSetupContext profilerPluginSetupContext = mock(ProfilerPluginSetupContext.class);
        when(profilerPluginSetupContext.getConfig()).thenReturn(new DefaultProfilerConfig());
        InstrumentContext instrumentContext = mock(InstrumentContext.class);
        elasticsearchPlugin.setTransformTemplate(new TransformTemplate(instrumentContext));
        try {
            elasticsearchPlugin.setup(profilerPluginSetupContext);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    @Test
    public void setTransformTemplate() {
        InstrumentContext instrumentContext = mock(InstrumentContext.class);
        elasticsearchPlugin.setTransformTemplate(new TransformTemplate(instrumentContext));
    }


    @Test
    public void testBaseClientTransformCallback() {
        ElasticsearchPlugin.BaseClientTransformCallback baseClientTransformCallback = mock(ElasticsearchPlugin.BaseClientTransformCallback.class);
        InstrumentClass instrumentClass = mock(InstrumentClass.class);
        try {
            baseClientTransformCallback.toBytecode(instrumentClass);
        }
        catch (Exception e){
            e.printStackTrace();
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testConfigRestClientTransformCallback() {
        ElasticsearchPlugin.ConfigRestClientTransformCallback configRestClientTransformCallback = mock(ElasticsearchPlugin.ConfigRestClientTransformCallback.class);
        Instrumentor instrumentor = mock(Instrumentor.class);
        Class classBeingRedefined = this.getClass();
        byte[] classfileBuffer = new byte[]{};
        ProtectionDomain protectionDomain = mock(ProtectionDomain.class);
        try {
            configRestClientTransformCallback.doInTransform(  instrumentor, this.getClass().getClassLoader(),
                    "org.frameworkset.elasticsearch.client.ConfigRestClientUtil", classBeingRedefined, protectionDomain,
             classfileBuffer) ;
        }
        catch (Exception e){
            e.printStackTrace();
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testRestClientTransformCallback() {
        ElasticsearchPlugin.RestClientTransformCallback restClientTransformCallback = mock(ElasticsearchPlugin.RestClientTransformCallback.class);
        Instrumentor instrumentor = mock(Instrumentor.class);
        Class classBeingRedefined = this.getClass();
        byte[] classfileBuffer = new byte[]{};
        ProtectionDomain protectionDomain = mock(ProtectionDomain.class);
        try {
            restClientTransformCallback.doInTransform(  instrumentor, this.getClass().getClassLoader(),
                    "org.frameworkset.elasticsearch.client.RestClientUtil", classBeingRedefined, protectionDomain,
                    classfileBuffer) ;
        }
        catch (Exception e){
            e.printStackTrace();
            Assert.assertNotNull(e);
        }
    }
    @Test
    public void testRestSearchExecutorTransformCallback() {
        ElasticsearchPlugin.RestSearchExecutorTransformCallback restSearchExecutorTransformCallback = mock(ElasticsearchPlugin.RestSearchExecutorTransformCallback.class);
        Instrumentor instrumentor = mock(Instrumentor.class);
        Class classBeingRedefined = this.getClass();
        byte[] classfileBuffer = new byte[]{};
        ProtectionDomain protectionDomain = mock(ProtectionDomain.class);
        try {
            restSearchExecutorTransformCallback.doInTransform(  instrumentor, this.getClass().getClassLoader(),
                    "org.frameworkset.elasticsearch.client.RestSearchExecutor", classBeingRedefined, protectionDomain,
                    classfileBuffer) ;
        }
        catch (Exception e){
            e.printStackTrace();
            Assert.assertNotNull(e);
        }
    }
    @Test
    public void testSliceRunTaskTransformCallback() {
        ElasticsearchPlugin.ParallelRunTaskTransformCallback sliceRunTaskTransformCallback = mock(ElasticsearchPlugin.ParallelRunTaskTransformCallback.class);
        Instrumentor instrumentor = mock(Instrumentor.class);
        Class classBeingRedefined = this.getClass();
        byte[] classfileBuffer = new byte[]{};
        ProtectionDomain protectionDomain = mock(ProtectionDomain.class);
        try {
            sliceRunTaskTransformCallback.doInTransform(  instrumentor, this.getClass().getClassLoader(),
                    "org.frameworkset.elasticsearch.SliceRunTask", classBeingRedefined, protectionDomain,
                    classfileBuffer) ;
        }
        catch (Exception e){
            e.printStackTrace();
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testScrollRunTaskTransformCallback() {
        ElasticsearchPlugin.ParallelRunTaskTransformCallback sliceRunTaskTransformCallback = mock(ElasticsearchPlugin.ParallelRunTaskTransformCallback.class);
        Instrumentor instrumentor = mock(Instrumentor.class);
        Class classBeingRedefined = this.getClass();
        byte[] classfileBuffer = new byte[]{};
        ProtectionDomain protectionDomain = mock(ProtectionDomain.class);
        try {
            sliceRunTaskTransformCallback.doInTransform(  instrumentor, this.getClass().getClassLoader(),
                    "org.frameworkset.elasticsearch.scroll.thread.ScrollTask", classBeingRedefined, protectionDomain,
                    classfileBuffer) ;
        }
        catch (Exception e){
            e.printStackTrace();
            Assert.assertNotNull(e);
        }
    }
}
