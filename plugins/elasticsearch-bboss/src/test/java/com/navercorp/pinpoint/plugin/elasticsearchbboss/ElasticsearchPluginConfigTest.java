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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchPluginConfigTest {
	private ElasticsearchPluginConfig elasticsearchPluginConfig = null;

	@Before
	public void setUp(){
		elasticsearchPluginConfig = new ElasticsearchPluginConfig(null);
	}

	@Test
    public void testIsEnabled() {
		Assert.assertFalse(elasticsearchPluginConfig.isEnabled());
	}

    @Test
    public void testToString() {
        Assert.assertNotNull(elasticsearchPluginConfig.toString());
    }
}
