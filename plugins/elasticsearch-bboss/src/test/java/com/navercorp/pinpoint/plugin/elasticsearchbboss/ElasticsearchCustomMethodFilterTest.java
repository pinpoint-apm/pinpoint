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


import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchCustomMethodFilterTest{

	@Test
	public void testReject() {
		ElasticsearchCustomMethodFilter elasticsearchCustomMethodFilter = new ElasticsearchCustomMethodFilter();
		InstrumentMethod instrumentMethod = new InstrumentMethodIT( "runSliceTask");
		Assert.assertFalse(elasticsearchCustomMethodFilter.accept(instrumentMethod));
	}

	@Test
	public void testAccept() {
		ElasticsearchCustomMethodFilter elasticsearchCustomMethodFilter = new ElasticsearchCustomMethodFilter();
		InstrumentMethod instrumentMethod = new InstrumentMethodIT( "execute");
		Assert.assertTrue(elasticsearchCustomMethodFilter.accept(instrumentMethod));

		instrumentMethod = new InstrumentMethodIT( "runSliceTask");
		Assert.assertFalse(elasticsearchCustomMethodFilter.accept(instrumentMethod));
	}

}
