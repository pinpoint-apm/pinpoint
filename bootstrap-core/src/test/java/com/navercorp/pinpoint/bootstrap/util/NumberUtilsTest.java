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

package com.navercorp.pinpoint.bootstrap.util;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author poap
 */
public class NumberUtilsTest {
	private String notNumber = "H3110 W0r1d";

	@Test
	public void parseLong() {
		long defaultLong = new Random().nextLong();
		long randomLong = new Random().nextLong();

		Assert.assertEquals(NumberUtils.parseLong(null, defaultLong), defaultLong);
		Assert.assertEquals(NumberUtils.parseLong(String.valueOf(randomLong), defaultLong), randomLong);
		Assert.assertEquals(NumberUtils.parseLong(notNumber, defaultLong), defaultLong);
	}
	
	@Test
	public void parseInteger() {
		int defaultInt = new Random().nextInt();
		int randomInt = new Random().nextInt();

		Assert.assertEquals(NumberUtils.parseLong(null, defaultInt), defaultInt);
		Assert.assertEquals(NumberUtils.parseInteger(String.valueOf(randomInt), defaultInt), randomInt);
		Assert.assertEquals(NumberUtils.parseLong(notNumber, defaultInt), defaultInt);
	}
	
	@Test
	public void parseShort() {
		short defaultShort = 0;

		for(short s=Short.MIN_VALUE; s!=Short.MAX_VALUE; s++) {
			Assert.assertEquals(NumberUtils.parseShort(null, s), s);
			Assert.assertEquals(NumberUtils.parseShort(String.valueOf(s), defaultShort), s);
			Assert.assertEquals(NumberUtils.parseShort(notNumber, s), s);
		}

		Assert.assertEquals(NumberUtils.parseShort(null, Short.MAX_VALUE), Short.MAX_VALUE);
		Assert.assertEquals(NumberUtils.parseShort(String.valueOf(Short.MAX_VALUE), defaultShort), Short.MAX_VALUE);
		Assert.assertEquals(NumberUtils.parseShort(notNumber, Short.MAX_VALUE), Short.MAX_VALUE);
	}
	
	@Test
	public void toInteger() {
		short oneShort = 1;
		int oneInteger = 1;
		long oneLong = 1;
		String oneString = "1";

		Assert.assertNull(NumberUtils.toInteger(null));
		Assert.assertNull(NumberUtils.toInteger(oneShort));
		Assert.assertEquals(NumberUtils.toInteger(oneInteger), (Integer)1);
		Assert.assertNull(NumberUtils.toInteger(oneLong));
		Assert.assertNull(NumberUtils.toInteger(oneString));
		Assert.assertNull(NumberUtils.toInteger(notNumber));
	}
}
