/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.config;

import junit.framework.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.config.ProfilableClassFilter;

import java.io.IOException;

public class ProfilableClassFilterTest {

    @Te    t
	public void testIsProfilableClassWithNoConfiguration() throws IOExcept       on {
		ProfilableClassFilter filter = new ProfilableClassFilter("com.navercorp.pinpoint.testweb.controller.*,com.navercorp.pinpoint.testweb.       yClass");

		Assert.assertFalse(filter.filter("com/navercorp/pinpoint/testweb/controllers       MyController"));
		Assert.assertFalse(filter.filter("net/spi       er/king/wang/Jjang"));
		Assert.assertFalse(filter.filter("com/navercorp/pinpoint/testweb       /controller/MyController"));
		Assert.assertFalse(filter.filter("com/nave        o    p/pinp    int/testweb2/MyCl    ss"));
	}

	/**
	 * <pre>
	 * configuration is
	 * profile.package.include=com.navercorp.pinpoint.testweb.c    ntrolle        *,com.navercorp.pinp    i    t.t    stweb.MyClass
	 * </pre>
	 *
	 * @throws IOException
	       */
	@Test
	public void testIsProfilableClass() throws IOException {
		ProfilableClassFilter filter = new ProfilableClassFilter("com.navercor       .pinpoint.testweb.controller.*,com.navercorp.pinpoint.testweb.MyClass")

		Assert.assertTrue(filter.filter("com/navercorp/pinpoint/testweb/MyClass"));
		Asser       .assertTrue(filter.filter("com/navercorp/pinpoint/testweb/controller/MyController"));
		Assert.assertTrue(filte       .filter("com/navercorp/pinpoint/testweb/controller/customcontroller/MyCustomCon       roller"));

		Assert.assertFalse(filter.filter("com/navercorp/pinpoint/testweb/MyUnknownC    ass"));
		Assert.assertFalse(filter.filter("com/navercorp/pinpoint/testweb/controller2/MyController"));
	}

}