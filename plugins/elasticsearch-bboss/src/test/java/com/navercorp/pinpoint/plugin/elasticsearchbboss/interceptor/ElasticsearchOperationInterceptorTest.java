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
package com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchOperationInterceptorTest {
    ElasticsearchOperationInterceptor elasticsearchExecutorOperationInterceptor = null;
    Object[] args = null;
    @Before
    public void setUp(){
        MethodDescriptor methodDescriptor = mock(MethodDescriptor.class);
        when(methodDescriptor.getClassName()).thenReturn(" ");
        TraceContext traceContext = mock(TraceContext.class);
        ProfilerConfig profilerConfig = mock(ProfilerConfig.class);
        when(traceContext.getProfilerConfig()).thenReturn(profilerConfig);
        elasticsearchExecutorOperationInterceptor = new ElasticsearchOperationInterceptor(traceContext, methodDescriptor);
        args = new Object[]{"1","2","3","4","5","6"};
    }
    @Test
    public void testConstruction(){
        MethodDescriptor methodDescriptor = mock(MethodDescriptor.class);
        when(methodDescriptor.getClassName()).thenReturn(" ");
        TraceContext traceContext = mock(TraceContext.class);
        ProfilerConfig profilerConfig = mock(ProfilerConfig.class);
        when(traceContext.getProfilerConfig()).thenReturn(profilerConfig);
        ElasticsearchOperationInterceptor elasticsearchExecutorOperationInterceptor = new ElasticsearchOperationInterceptor(traceContext, methodDescriptor);
    }
    @Test
    public void testBefore(){


        try {
            elasticsearchExecutorOperationInterceptor.before(new Object(),args);
        }
        catch (Exception e){
            Assert.assertTrue(e instanceof NullPointerException);
        }

    }

    @Test
    public void testAfter(){


        try {
            elasticsearchExecutorOperationInterceptor.after(new Object(), args, "ok", null);
        }
        catch (Exception e){
            Assert.assertTrue(e instanceof NullPointerException);
        }
    }







    @Test
    public void doInEventAfterTrace() {
        try {

            elasticsearchExecutorOperationInterceptor.doInAfterTrace(mock(SpanEventRecorder.class),new Object(),args,"aa",null);
        }
        catch (Exception e){

        }

    }



}
