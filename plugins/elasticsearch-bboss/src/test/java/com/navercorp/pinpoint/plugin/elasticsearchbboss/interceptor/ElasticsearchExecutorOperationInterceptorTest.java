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

package com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchExecutorOperationInterceptorTest {
	ElasticsearchExecutorOperationInterceptor elasticsearchExecutorOperationInterceptor = null;
	Object[] args = null;
	@Before
	public void setUp(){
		elasticsearchExecutorOperationInterceptor = new ElasticsearchExecutorOperationInterceptor(new TraceContextIT(), new MethodDescriptorIT());
		args = new Object[]{"1","2","3","4","5","6"};
	}
	@Test
	public void testConstruction(){
		ElasticsearchExecutorOperationInterceptor elasticsearchExecutorOperationInterceptor = new ElasticsearchExecutorOperationInterceptor(new TraceContextIT(), new MethodDescriptorIT());
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
	public void testDoSpanInBeforeTrace() {
		try {
			elasticsearchExecutorOperationInterceptor.doInBeforeTrace(new SpanRecorderIT(), new Object(), args, true);
		}
		catch (Exception e){

		}
	}


	@Test
	public void testConvertParams(){
		try {
			String value = elasticsearchExecutorOperationInterceptor.convertParams(args);
		}
		catch (Exception e){

		}
	}



	@Test
	public void doInEventAfterTrace() {
		try {
			elasticsearchExecutorOperationInterceptor.doInAfterTrace(new SpanEventRecorderIT(),new Object(),args,"aa",null,true);
		}
		catch (Exception e){

		}

	}

	@Test
	public void doInAfterTrace() {
		try {
			elasticsearchExecutorOperationInterceptor.doInAfterTrace(new SpanRecorderIT(),new Object(),args,"aa",null);
		}
		catch (Exception e){

		}
	}

}
