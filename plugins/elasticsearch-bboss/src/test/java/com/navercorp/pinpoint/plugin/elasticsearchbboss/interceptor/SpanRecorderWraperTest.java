package com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor;
/**
 * Copyright 2008 biaoping.yin
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/12/9 21:32
 * @author biaoping.yin
 * @version 1.0
 */
public class SpanRecorderWraperTest {
	private RecorderWraper spanEventRecoder;
	@Before
	public void setUp(){
		spanEventRecoder = new SpanRecorderWraper(new SpanRecorderIT());
	}
	@Test
	public void recordAttributeString( ){
		spanEventRecoder.recordAttribute(AnnotationKey.ARGS0,"test");
	}
	@Test
	public void recordAttributeInt( ){
		spanEventRecoder.recordAttribute(AnnotationKey.ARGS0,1);
	}
	@Test
	public void recordAttributeObject( ){
		spanEventRecoder.recordAttribute(AnnotationKey.ARGS0,1);
	}
	@Test
	public  void recordApi( ){
		spanEventRecoder.recordApi(new MethodDescriptorIT());
	}

	public void recordServiceType(){
		spanEventRecoder.recordServiceType(null);
	}
	public void recordException(){
		spanEventRecoder.recordException(null);
	}
}
