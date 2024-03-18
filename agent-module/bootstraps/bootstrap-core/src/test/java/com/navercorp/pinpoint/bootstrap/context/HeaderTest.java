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

package com.navercorp.pinpoint.bootstrap.context;

import com.navercorp.pinpoint.common.util.DelegateEnumeration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author jaehong.kim
 */
public class HeaderTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void testToString() {
        logger.debug("{}", Header.HTTP_FLAGS);
    }

    @Test
    public void isHeaderKey() {
        Assertions.assertTrue(Header.startWithPinpointHeader(Header.HTTP_FLAGS.toString()));
        Assertions.assertFalse(Header.startWithPinpointHeader("Not_Exist"));
        Assertions.assertFalse(Header.startWithPinpointHeader(null));
    }

    @Test
    public void filteredHeaderNames() {
        Hashtable<String, String> hashtable = new Hashtable<>();
        hashtable.put("a", "aa");
        hashtable.put("b", Header.HTTP_FLAGS.toString());
        hashtable.put("c", "cc");
        Enumeration<String> elements = hashtable.elements();

        Enumeration enumeration = new DelegateEnumeration(elements, Header.FILTER);
        int count = 0;
        while (enumeration.hasMoreElements()) {
            count++;
            Assertions.assertFalse(Header.startWithPinpointHeader((String) enumeration.nextElement()));
        }
        Assertions.assertEquals(count, 2);
    }

    @Test
    public void startWithPinpointHeader() {
        Assertions.assertTrue(Header.startWithPinpointHeader("Pinpoint-Unknown"));
        Assertions.assertTrue(Header.startWithPinpointHeader("pinpoint-unknown"));
        Assertions.assertFalse(Header.startWithPinpointHeader("unknown-pinpoint"));
        Assertions.assertFalse(Header.startWithPinpointHeader("unknown"));
    }

}