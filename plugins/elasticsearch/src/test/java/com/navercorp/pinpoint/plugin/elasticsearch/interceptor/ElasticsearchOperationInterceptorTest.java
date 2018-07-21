/*
 * Copyright 2016 Pinpoint contributors and NAVER Corp.
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

package com.navercorp.pinpoint.plugin.elasticsearch.interceptor;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchOperationInterceptorTest {

    @Test
    public void testBefore(){
        try {
            ElasticsearchOperationInterceptor elasticsearchExecutorOperationInterceptor = new ElasticsearchOperationInterceptor(null, null);
        }
        catch (Exception e){
            Assert.assertEquals("traceContext must not be null",e.getMessage());
        }

        try {
            ElasticsearchOperationInterceptor elasticsearchExecutorOperationInterceptor = new ElasticsearchOperationInterceptor();
            elasticsearchExecutorOperationInterceptor.before(null,null);
        }
        catch (Exception e){
            Assert.assertTrue(e instanceof NullPointerException);
        }

    }

    @Test
    public void testAfter(){
        try {
            ElasticsearchExecutorOperationInterceptor elasticsearchExecutorOperationInterceptor = new ElasticsearchExecutorOperationInterceptor(null, null);
        }
        catch (Exception e){
            Assert.assertEquals("traceContext must not be null",e.getMessage());
        }

        try {
            ElasticsearchExecutorOperationInterceptor elasticsearchExecutorOperationInterceptor = new ElasticsearchExecutorOperationInterceptor();
            elasticsearchExecutorOperationInterceptor.after(null,null,null,null);
        }
        catch (Exception e){
            Assert.assertTrue(e instanceof NullPointerException);
        }
    }

}
