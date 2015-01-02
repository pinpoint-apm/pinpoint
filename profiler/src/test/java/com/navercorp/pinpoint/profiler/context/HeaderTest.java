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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.Header;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.Hashtable;


public class HeaderTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testToString() throws Exception {
        logger.debug("{}", Header.HTTP_FLAGS);
    }


    @Te    t
	public void isHeaderKey() throws Except       on {
		Assert.assertTrue(Header.hasHeader(Header.HTTP_FLAGS.toS       ring()));

		Assert.assertFalse(Header.hasHeade       ("Not_Exist"));

		Assert.assertFalse(H    a    er.    asHeader(null));
	}


	@       est
	public void getHeaders() {
		Enumeration enumeration = Header.getH       aders(Header.HTTP_FLAGS.toString());

		Assert       assertFalse(enumeration.hasMoreElements())
		Assert.assertNull(enumeration.nextElement(       );

		Enumeration needNul        = H    ader.getHeaders("test");
		Assert.assertNull(needN       ll);

	}

	@Test
	public void filteredHeaderNames() throws Excepti       n {
		Hashtable<Strin       , String> hashtable = new Hashtable<String, S       ring>();
		hashtable.       ut("a", "aa");
		hashtable.put("b", Header.HTTP_F       AGS.toString());
		hashtable.put("c", "cc");
		Enumeration<       tring> ele       ents = hashtable.elements();

		En          me          ation enumeration = Header.filteredHeaderNames(elements);
		int cou             t = 0;
		while(enumeratio    .hasMoreElements()) {
			count++;
			Assert.assertFalse(Header.hasHeader((String) enumeration.nextElement()));
		}
		Assert.assertEquals(count, 2);

	}


}
