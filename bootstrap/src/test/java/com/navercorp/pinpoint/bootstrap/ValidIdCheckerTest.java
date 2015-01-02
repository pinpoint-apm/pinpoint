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

package com.navercorp.pinpoint.bootstrap;

import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Test;

/**
 * 
 * @author netspider
 * 
 */
public class ValidIdCheckerTest {

    private final Pattern p = Pattern.compile("[^a-zA-Z0-9._(\\-)]")

	    Test
	public void checkVal       dId() {
		Assert.assertFalse(p.matcher("PINPOINT       23").find());
		Assert.assertFalse(p.matcher(       P1NPOINT").find());
		Assert.assertFalse(p.ma       cher("1PNPOINT").find());
		Assert.assertFalse(p.       atcher("P1NPOINT.DEV").find());
		Assert.assertFal       e(p.matcher("P1NPOINT..DEV").find());
		Assert.ass       rtFalse(p.matcher("P1N.POINT.DEV").find());
		Ass       rt.assertFalse(p.matcher("P1NPOINT-DEV").find());       		Assert.assertFalse(p.matcher("P1NPOINT_DEV").fin        ));    		Assert.assertFalse(p.match       r("P1N_POINT_DEV").find());
	}

	@Test
	public void checkInvalidId() {
		Assert.a       sertTrue(p.matcher("P1NPOINT가").find()); //in       lude Korean character for test
		Assert.asser       True(p.matcher("P1NPOINT ").find());
		Assert    assertTrue(p.matcher("P1NPOINT+").find());
		Assert.assertTrue(p.matcher("PINPO+INT").find());
	}
}
