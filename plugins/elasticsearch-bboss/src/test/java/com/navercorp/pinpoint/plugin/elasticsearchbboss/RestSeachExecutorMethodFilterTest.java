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
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author yinbp[yin-bp@163.com]
 */
public class RestSeachExecutorMethodFilterTest {
	private RestSeachExecutorMethodFilter restSeachExecutorMethodFilter;
	@Before
	public void setUp(){
		restSeachExecutorMethodFilter = new RestSeachExecutorMethodFilter();
	}
	@Test
	public void testAccept() {
		InstrumentMethod instrumentMethod = mock(InstrumentMethod.class);
		when(instrumentMethod.getName()).thenReturn("executeSimpleRequest");
		Assert.assertTrue(restSeachExecutorMethodFilter.accept(instrumentMethod));


	}
}
