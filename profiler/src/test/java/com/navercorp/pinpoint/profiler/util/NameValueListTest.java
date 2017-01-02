/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author poap
 */
public class NameValueListTest {
	private NameValueList<Integer> list;

	@Before
	public void beforeTest() {
		list = new NameValueList<Integer>();
		list.add("one", 1);
		list.add("two", 2);
		list.add("three", 3);
	}
	
	@Test
	public void add() {
		Assert.assertEquals(list.add("one", 11).intValue(), 1);
		Assert.assertEquals(list.add("two", 22).intValue(), 2);
		Assert.assertEquals(list.add("three", 33).intValue(), 3);
		Assert.assertNull(list.add("four", 4));
		Assert.assertEquals(list.add("one", 111).intValue(), 11);
		Assert.assertEquals(list.add("two", 222).intValue(), 22);
		Assert.assertEquals(list.add("three", 333).intValue(), 33);
		Assert.assertEquals(list.add("four", 44).intValue(), 4);
		Assert.assertNull(list.add("five", 5));
	}
	
	@Test
	public void get() {
		Assert.assertEquals(list.get("one").intValue(), 1);
		Assert.assertEquals(list.get("two").intValue(), 2);
		Assert.assertEquals(list.get("three").intValue(), 3);
		Assert.assertNull(list.get("four"));
	}
	
	@Test
	public void remove() {
		Assert.assertEquals(list.remove("one").intValue(), 1);
		Assert.assertEquals(list.remove("two").intValue(), 2);
		Assert.assertEquals(list.remove("three").intValue(), 3);
		Assert.assertNull(list.remove("four"));
		Assert.assertNull(list.remove("three"));
		Assert.assertNull(list.remove("two"));
		Assert.assertNull(list.remove("four"));
	}
    
	@Test
    public void clear() {
		list.clear();
		Assert.assertNull(list.get("one"));
		Assert.assertNull(list.get("two"));
		Assert.assertNull(list.get("three"));
    }
}
