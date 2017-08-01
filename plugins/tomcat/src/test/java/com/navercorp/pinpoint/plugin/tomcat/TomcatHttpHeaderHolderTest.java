/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.tomcat;

import com.navercorp.pinpoint.bootstrap.context.Header;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.Hashtable;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class TomcatHttpHeaderHolderTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void isHeaderKey() throws Exception {
        Assert.assertTrue(TomcatHttpHeaderHolder.hasHeader(Header.HTTP_FLAGS.toString()));

        Assert.assertFalse(TomcatHttpHeaderHolder.hasHeader("Not_Exist"));

        Assert.assertFalse(TomcatHttpHeaderHolder.hasHeader(null));
    }


    @Test
    public void getHeaders() {
        Enumeration enumeration = TomcatHttpHeaderHolder.getHeaders(Header.HTTP_FLAGS.toString());

        Assert.assertFalse(enumeration.hasMoreElements());
        Assert.assertNull(enumeration.nextElement());

        Enumeration needNull = TomcatHttpHeaderHolder.getHeaders("test");
        Assert.assertNull(needNull);

    }

    @Test
    public void filteredHeaderNames() throws Exception {
        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        hashtable.put("a", "aa");
        hashtable.put("b", Header.HTTP_FLAGS.toString());
        hashtable.put("c", "cc");
        Enumeration<String> elements = hashtable.elements();

        Enumeration enumeration = TomcatHttpHeaderHolder.filteredHeaderNames(elements);
        int count = 0;
        while(enumeration.hasMoreElements()) {
            count++;
            Assert.assertFalse(TomcatHttpHeaderHolder.hasHeader((String) enumeration.nextElement()));
        }
        Assert.assertEquals(count, 2);

    }

}