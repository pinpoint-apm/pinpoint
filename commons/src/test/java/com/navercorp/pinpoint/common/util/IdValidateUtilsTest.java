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

package com.navercorp.pinpoint.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IdValidateUtilsTest {

    @Test
    public void testValidateId() {
        Assertions.assertTrue(IdValidateUtils.validateId("abcd"));
        Assertions.assertTrue(IdValidateUtils.validateId("ab-_bc"));
        Assertions.assertTrue(IdValidateUtils.validateId("test.abc"));

        Assertions.assertTrue(IdValidateUtils.validateId("--__"));

        Assertions.assertFalse(IdValidateUtils.validateId("()"));

        Assertions.assertTrue(IdValidateUtils.validateId("."));
        Assertions.assertFalse(IdValidateUtils.validateId(""));


        Assertions.assertFalse(IdValidateUtils.validateId("한글")); // test with parameter written in Korean.
    }

    @Test
    public void testValidateId_max_length() {
        Assertions.assertTrue(IdValidateUtils.validateId("0123456789012"), "check max length");
        Assertions.assertTrue(IdValidateUtils.validateId("012345678901234567891234"), "check max length");

        Assertions.assertFalse(IdValidateUtils.validateId("0123456789012345678912345"), "check max length");
        Assertions.assertFalse(IdValidateUtils.validateId("0123456789012345678912345"), "check max length");
        Assertions.assertFalse(IdValidateUtils.validateId(""), "empty");
    }

    @Test
    public void testValidateId_custom_max_length() {
        Assertions.assertTrue(IdValidateUtils.validateId("0", 1), "check max length");
        Assertions.assertFalse(IdValidateUtils.validateId("01", 1), "check max length");

        Assertions.assertTrue(IdValidateUtils.validateId("0", 2), "check max length");
        Assertions.assertFalse(IdValidateUtils.validateId("0123", 2), "check max length");


        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            IdValidateUtils.validateId("0123", -1);
        });
    }

    @Test
    public void testCheckUrlPattern() {
        Assertions.assertTrue(IdValidateUtils.checkUrlPattern("abcd"));
        Assertions.assertTrue(IdValidateUtils.checkUrlPattern("host.example.com:8080"));
        Assertions.assertTrue(IdValidateUtils.checkUrlPattern("http://user:pw@host.example.com:8080/path/to/resource?query=1&x=y#frag"));
        Assertions.assertTrue(IdValidateUtils.checkUrlPattern("http://[::1]:8080/a%20b"));
        Assertions.assertTrue(IdValidateUtils.checkUrlPattern("~!$&'()*+,;="));

        Assertions.assertFalse(IdValidateUtils.checkUrlPattern(""));
        Assertions.assertFalse(IdValidateUtils.checkUrlPattern(" "));
        Assertions.assertFalse(IdValidateUtils.checkUrlPattern("a b"));
        Assertions.assertFalse(IdValidateUtils.checkUrlPattern("a\tb"));
        Assertions.assertFalse(IdValidateUtils.checkUrlPattern("a\"b"));
        Assertions.assertFalse(IdValidateUtils.checkUrlPattern("a<b>"));
        Assertions.assertFalse(IdValidateUtils.checkUrlPattern("a\\b"));
        Assertions.assertFalse(IdValidateUtils.checkUrlPattern("a{b}"));
        Assertions.assertFalse(IdValidateUtils.checkUrlPattern("a|b"));
        Assertions.assertFalse(IdValidateUtils.checkUrlPattern("a^b"));
        Assertions.assertFalse(IdValidateUtils.checkUrlPattern("a`b"));
        Assertions.assertFalse(IdValidateUtils.checkUrlPattern("한글"));
        Assertions.assertFalse(IdValidateUtils.checkUrlPattern("a\u0000b"));
    }

    @Test
    public void testCheckId_min_length() {
        Assertions.assertEquals(IdValidateUtils.CheckResult.FAIL_LENGTH, IdValidateUtils.checkId("ab", 3, 10));
        Assertions.assertEquals(IdValidateUtils.CheckResult.SUCCESS, IdValidateUtils.checkId("abc", 3, 10));
        Assertions.assertEquals(IdValidateUtils.CheckResult.SUCCESS, IdValidateUtils.checkId("abcdefghij", 3, 10));
        Assertions.assertEquals(IdValidateUtils.CheckResult.FAIL_LENGTH, IdValidateUtils.checkId("abcdefghijk", 3, 10));
        Assertions.assertEquals(IdValidateUtils.CheckResult.FAIL_PATTERN, IdValidateUtils.checkId("a b", 3, 10));

        Assertions.assertTrue(IdValidateUtils.validateId("abc", 3, 10));
        Assertions.assertFalse(IdValidateUtils.validateId("ab", 3, 10));

        // the 2-arg form keeps rejecting the empty string
        Assertions.assertEquals(IdValidateUtils.CheckResult.FAIL_LENGTH, IdValidateUtils.checkId("", 10));
        Assertions.assertTrue(IdValidateUtils.checkLength("a", 10));
        Assertions.assertFalse(IdValidateUtils.checkLength("", 10));
    }

    @Test
    public void testCheckId_invalid_bounds() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> IdValidateUtils.checkId("abc", 0, 10));
        Assertions.assertThrows(IllegalArgumentException.class, () -> IdValidateUtils.checkId("abc", 5, 4));
    }

    @Test
    public void testValidateId_offset() {
        String postFix = "agent^";
        Assertions.assertTrue(IdValidateUtils.checkIdPattern(postFix, 0, postFix.length() - 1));

        String preFix = "^agent";
        Assertions.assertTrue(IdValidateUtils.checkIdPattern(preFix, 1, preFix.length()));

        String all = "^agent^";
        Assertions.assertTrue(IdValidateUtils.checkIdPattern(all, 1, all.length() - 1));

        String error = "^age&nt&";
        Assertions.assertFalse(IdValidateUtils.checkIdPattern(error, 1, error.length()));

    }

}