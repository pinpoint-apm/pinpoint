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
package com.navercorp.pinpoint.plugin.elasticsearchbboss;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchInterceptorClassInfoTest {
	private ElasticsearchInterceptorClassInfo elasticsearchInterceptorClassInfo = null;

	@Before
	public void setUp(){
		elasticsearchInterceptorClassInfo = new ElasticsearchInterceptorClassInfo();
	}
	@Test
	public void testGetInterceptorMehtods() {
		Assert.assertNull(elasticsearchInterceptorClassInfo.getInterceptorMehtods());
	}
	@Test
	public void testSetInterceptorMehtods() {
		this.elasticsearchInterceptorClassInfo.setInterceptorMehtods(null);
		Assert.assertNull(elasticsearchInterceptorClassInfo.getInterceptorMehtods());
	}
	@Test
	public void testGetInterceptorClass() {
		Assert.assertNull(elasticsearchInterceptorClassInfo.getInterceptorClass());
	}

	@Test
	public void testSetInterceptorClass( ) {
		this.elasticsearchInterceptorClassInfo.setInterceptorClass(null);
		Assert.assertNull(elasticsearchInterceptorClassInfo.getInterceptorClass());
	}
	@Test
	public void testGetMethodFilter() {
		Assert.assertNull(elasticsearchInterceptorClassInfo.getMethodFilter());
	}
	@Test
	public void testSetMethodFilter( ) {
		this.elasticsearchInterceptorClassInfo.setMethodFilter(null);
		Assert.assertNull(elasticsearchInterceptorClassInfo.getMethodFilter());
	}
	@Test
	public void testGetAllAccept() {
		Assert.assertNull(elasticsearchInterceptorClassInfo.getAllAccept());
	}
	@Test
	public void testSetAllAccept() {
		this.elasticsearchInterceptorClassInfo.setAllAccept(null);
		Assert.assertNull(elasticsearchInterceptorClassInfo.getAllAccept());
	}
	@Test
	public void testGetAllReject() {
		Assert.assertNull(elasticsearchInterceptorClassInfo.getAllReject());
	}
	@Test
	public void testSetAllReject() {
		this.elasticsearchInterceptorClassInfo.setAllReject(null);
		Assert.assertNull(elasticsearchInterceptorClassInfo.getAllReject());
	}
}
