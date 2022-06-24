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


        try {
            IdValidateUtils.validateId("0123", -1);
            Assertions.fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testValidateId_offset() {
        String postFix = "agent^";
        Assertions.assertTrue(IdValidateUtils.checkId(postFix, 0, postFix.length() - 1));

        String preFix = "^agent";
        Assertions.assertTrue(IdValidateUtils.checkId(preFix, 1, preFix.length()));

        String all = "^agent^";
        Assertions.assertTrue(IdValidateUtils.checkId(all, 1, all.length() - 1));

        String error = "^age&nt&";
        Assertions.assertFalse(IdValidateUtils.checkId(error, 1, error.length()));

    }

}