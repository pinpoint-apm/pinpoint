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

package com.navercorp.pinpoint.profiler;

import junit.framework.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.profiler.ClassFileFilter;
import com.navercorp.pinpoint.profiler.DefaultClassFileFilter;

import java.net.URL;
import java.net.URLClassLoader;


public class DefaultClassFileFilterTest {

    @Te    t
	public void testDoFilter_Package() throws Except       on {
		ClassFileFilter filter = new DefaultClassFileFilter(this.getClass().getClass       oader());

		Assert.assertTrue(filter.doFilter(null, "java/test", nu       l, null, null));
		Assert.assertTrue(filter.doFilter(null, "javax/tes       ", null, null, null));
		Assert.assertTrue(filter.doFilter(null, "com/navercorp/pin       oint/", null, null, null));

		Assert.assertFalse(filter.doFilte    (    ull     "test", null, null, null));
	}


	@Test
	public void t       stDoFilter_ClassLoader() throws Exception {
		ClassFileFilter filter = new DefaultCl       ssFileFilter(this.getClass().getClassLoader());


		Assert.assertTrue(filter.doFilter(this.g       tClass().getClassLoader(), "test", null, null, null));

	       URLClassLoader classLoader = new URLClassLoader(new URL[]{});
		Assert.    ssertFalse(filter.doFilter(classLoader, "test", null, null, null));
	}
}