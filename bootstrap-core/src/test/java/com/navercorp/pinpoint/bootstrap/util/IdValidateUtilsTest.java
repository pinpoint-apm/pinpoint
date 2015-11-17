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

import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.util.IdValidateUtils;

public class IdValidateUtilsTest  {

    @Test
    public void testValidateId() throws Exception {
        Assert.assertTrue(IdValidateUtils.validateId("abcd"));
        Assert.assertTrue(IdValidateUtils.validateId("ab-_bc"));
        Assert.assertTrue(IdValidateUtils.validateId("test.abc"));

        Assert.assertTrue(IdValidateUtils.validateId("--__"));

        Assert.assertFalse(IdValidateUtils.validateId("()"));

        Assert.assertTrue(IdValidateUtils.validateId("."));
        Assert.assertFalse(IdValidateUtils.validateId(""));


        Assert.assertFalse(IdValidateUtils.validateId("한글")); // test with parameter written in Korean.
    }

    @Test
    public void testValidateId_max_length() throws Exception {
        Assert.assertTrue("check max length", IdValidateUtils.validateId("0123456789012"));
        Assert.assertTrue("check max length", IdValidateUtils.validateId("012345678901234567891234"));

        Assert.assertFalse("check max length", IdValidateUtils.validateId("0123456789012345678912345"));
        Assert.assertFalse("check max length", IdValidateUtils.validateId("0123456789012345678912345"));
        Assert.assertFalse("empty", IdValidateUtils.validateId(""));
    }

    @Test
    public void testValidateId_custom_max_length() throws Exception {
        Assert.assertTrue("check max length", IdValidateUtils.validateId("0", 1));
        Assert.assertFalse("check max length", IdValidateUtils.validateId("01", 1));

        Assert.assertTrue("check max length", IdValidateUtils.validateId("0", 2));
        Assert.assertFalse("check max length", IdValidateUtils.validateId("0123", 2));


        try {
            IdValidateUtils.validateId("0123", -1);
            Assert.fail();
        } catch (IllegalArgumentException ignored) {
        }
    }
}