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

package com.navercorp.pinpoint.profiler.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author emeroad
 */
public class QueryStringUtilTest {
    @Test
    public void testRemoveAllMultiSpace() throws Exception {
        String s = QueryStringUtil.removeAllMultiSpace("a   b");

        Assertions.assertEquals("a b", s);
    }

    @Test
    public void testRemoveCarriageReturn() {
        String testStr;
        // single carriage return
        testStr = "\r";
        Assertions.assertEquals(" ", QueryStringUtil.removeCarriageReturn(testStr));
        // single line feed
        testStr = "\n";
        Assertions.assertEquals(" ", QueryStringUtil.removeCarriageReturn(testStr));
        // combined CRLF
        testStr = "\r\r\n\n\r\r\n";
        Assertions.assertEquals("       ", QueryStringUtil.removeCarriageReturn(testStr));
        // random string CRLF -> space
        testStr = "this is awesome CRs\r\rand awesome LFs\n\nwhat if we combine it?\r\n\r\n\r\nwow\r\nso standard\n\rmuch naver\n";
        Assertions.assertEquals("this is awesome CRs  and awesome LFs  what if we combine it?      wow  so standard  much naver ", QueryStringUtil.removeCarriageReturn(testStr));
    }

    @Test
    public void testRemoveAllMultiSpace2() {
        String testStr;
        // single spaces
        testStr = " ";
        Assertions.assertEquals(" ", QueryStringUtil.removeAllMultiSpace(testStr));
        // multiple spaces
        testStr = "        ";
        Assertions.assertEquals(" ", QueryStringUtil.removeAllMultiSpace(testStr));
        // random string with left/right side multiple space
        testStr = "        you may want to strip this message XD             ";
        Assertions.assertEquals(" you may want to strip this message XD ", QueryStringUtil.removeAllMultiSpace(testStr));
        // random string with multiple spaces
        testStr = "    bad        example  of     design     by making      hu        ge    s p  a   c     e           ";
        Assertions.assertEquals(" bad example of design by making hu ge s p a c e ", QueryStringUtil.removeAllMultiSpace(QueryStringUtil.removeCarriageReturn(testStr)));
    }
}
