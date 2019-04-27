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

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.navercorp.pinpoint.plugin.elasticsearchbboss.ElasticsearchConstants.ELASTICSEARCH_Parallel_SCOPE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class SliceWorkerRunInterceptorTest {

    ParallelWorkerRunInterceptor sliceWorkerRunInterceptor = null;
    Object[] args = null;
    @Before
    public void setUp(){
        sliceWorkerRunInterceptor = new ParallelWorkerRunInterceptor(mock(TraceContext.class), mock(MethodDescriptor.class));
        args = new Object[]{"1","2","3","4","5","6"};
    }

    @Test
    public void testConstruction(){
        InterceptorScope interceptorScopeIT = mock(InterceptorScope.class);
        when(interceptorScopeIT.getName()).thenReturn(ELASTICSEARCH_Parallel_SCOPE );
        ParallelWorkerRunInterceptor sliceWorkerRunInterceptor = new ParallelWorkerRunInterceptor(mock(TraceContext.class), mock(MethodDescriptor.class));
        Assert.assertNotNull(sliceWorkerRunInterceptor);
    }
    @Test
    public void beforeTest() {
        try {
            sliceWorkerRunInterceptor.before(new Object(),args);
        }
        catch (Exception e){
            Assert.assertTrue(e instanceof NullPointerException);
        }
    }

    @Test
    public void afterTest() {
        try {
            sliceWorkerRunInterceptor.after(new Object(),args,null,null);
        }
        catch (Exception e){
            Assert.assertTrue(e instanceof NullPointerException);
        }
    }

    @Test
    public void doInBeforeTrace() {
        try {
            sliceWorkerRunInterceptor.doInBeforeTrace(mock( SpanEventRecorder.class), mock(AsyncContext.class), new Object(),args);
        }
        catch (Exception e){

        }
    }

    @Test
    public void doInAfterTrace() {
        try {
            sliceWorkerRunInterceptor.doInAfterTrace(mock( SpanEventRecorder.class),  new Object(),args,null,null);
        }
        catch (Exception e){

        }
    }
}
