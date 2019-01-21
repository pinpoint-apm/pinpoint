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

import com.navercorp.pinpoint.plugin.elasticsearchbboss.AsyncContextIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class SliceWorkerRunInterceptorTest {

    SliceWorkerRunInterceptor sliceWorkerRunInterceptor = null;
    Object[] args = null;
    @Before
    public void setUp(){
        sliceWorkerRunInterceptor = new SliceWorkerRunInterceptor(new TraceContextIT(), new MethodDescriptorIT());
        args = new Object[]{"1","2","3","4","5","6"};
    }

    @Test
    public void testConstruction(){
        InterceptorScopeIT interceptorScopeIT = new InterceptorScopeIT("ElasticsearchBBoss_SLICE_SCOPE");
        SliceWorkerRunInterceptor sliceWorkerRunInterceptor = new SliceWorkerRunInterceptor(new TraceContextIT(), new MethodDescriptorIT());
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
            sliceWorkerRunInterceptor.doInBeforeTrace(new SpanEventRecorderIT(), new AsyncContextIT(), new Object(),args);
        }
        catch (Exception e){

        }
    }

    @Test
    public void doInAfterTrace() {
        try {
            sliceWorkerRunInterceptor.doInAfterTrace(new SpanEventRecorderIT(),  new Object(),args,null,null);
        }
        catch (Exception e){

        }
    }
}
