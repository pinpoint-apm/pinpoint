/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase.namespace;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author HyunGil Jeong
 */
public class HbaseNamespaceValidatorTest {

    @Test
    public void testValidate() {
        String valid1 = "a";
        String valid2 = "_a0";
        String valid3 = "0_Z";
        String valid4 = "C09_";

        String invalid1 = "";
        String invalid2 = "-";
        String invalid3 = "abc-1";
        String invalid4 = "abc!";
        String invalid5 = null;

        Assert.assertTrue(valid1 + " should be valid.", HbaseNamespaceValidator.INSTANCE.validate(valid1));
        Assert.assertTrue(valid2 + " should be valid.", HbaseNamespaceValidator.INSTANCE.validate(valid2));
        Assert.assertTrue(valid3 + " should be valid.", HbaseNamespaceValidator.INSTANCE.validate(valid3));
        Assert.assertTrue(valid4 + " should be valid.", HbaseNamespaceValidator.INSTANCE.validate(valid4));

        Assert.assertFalse(invalid1 + " should be invalid.", HbaseNamespaceValidator.INSTANCE.validate(invalid1));
        Assert.assertFalse(invalid2 + " should be invalid.", HbaseNamespaceValidator.INSTANCE.validate(invalid2));
        Assert.assertFalse(invalid3 + " should be invalid.", HbaseNamespaceValidator.INSTANCE.validate(invalid3));
        Assert.assertFalse(invalid4 + " should be invalid.", HbaseNamespaceValidator.INSTANCE.validate(invalid4));
        Assert.assertFalse(invalid4 + " should be invalid.", HbaseNamespaceValidator.INSTANCE.validate(invalid5));
    }
}
