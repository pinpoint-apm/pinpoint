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
package com.navercorp.pinpoint.plugin.elasticsearch;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchMethodInfoTest {
	private ElasticsearchMethodInfo elasticsearchMethodInfo = null;
	@Before
	public void setUp(){
		elasticsearchMethodInfo = new ElasticsearchMethodInfo();
	}
	@Test
	public void testGetFilterType() {
		Assert.assertEquals(0,elasticsearchMethodInfo.getFilterType());
	}
	@Test
	public void testSetFilterType() {
		elasticsearchMethodInfo.setFilterType(1);
		Assert.assertEquals(1,elasticsearchMethodInfo.getFilterType());
	}
	@Test
	public void testGetName() {
		Assert.assertNull(elasticsearchMethodInfo.getName());
	}
	@Test
	public void testSetName() {
		elasticsearchMethodInfo.setName("aaa");
		Assert.assertEquals("aaa",elasticsearchMethodInfo.getName());
	}
	@Test
	public void testIsPattern() {
		Assert.assertFalse(elasticsearchMethodInfo.isPattern());
	}
	@Test
	public void testSetPattern() {
		elasticsearchMethodInfo.setPattern(true);
		Assert.assertTrue(elasticsearchMethodInfo.isPattern());
	}
}
