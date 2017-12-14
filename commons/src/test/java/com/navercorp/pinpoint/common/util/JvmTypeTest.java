/*
 * Copyright 2016 Naver Corp.
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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author HyunGil Jeong
 */
public class JvmTypeTest {

    @Test
    public void fromVendorNullParameter() {
        JvmType actualType = JvmType.fromVendor(null);
        Assert.assertSame(JvmType.UNKNOWN, actualType);
    }

    @Test
    public void fromVendorEmptyParameter() {
        JvmType actualType = JvmType.fromVendor("");
        Assert.assertSame(JvmType.UNKNOWN, actualType);
    }

    @Test
    public void fromVendorValidParameter() {
        Assert.assertSame(JvmType.IBM, JvmType.fromVendor("IBM"));
        Assert.assertSame(JvmType.IBM, JvmType.fromVendor("ibm"));
        Assert.assertSame(JvmType.ORACLE, JvmType.fromVendor("Oracle"));
        Assert.assertSame(JvmType.ORACLE, JvmType.fromVendor("oracle"));
        Assert.assertSame(JvmType.OPENJDK, JvmType.fromVendor("OpenJDK"));
        Assert.assertSame(JvmType.OPENJDK, JvmType.fromVendor("openjdk"));
    }

    @Test
    public void fromVendorInvalidParameter() {
        Assert.assertSame(JvmType.UNKNOWN, JvmType.fromVendor("Some Invalid Parameter"));
    }

    @Test
    public void fromVmNameNullParameter() {
        JvmType actualType = JvmType.fromVendor(null);
        Assert.assertSame(JvmType.UNKNOWN, actualType);
    }

    @Test
    public void fromVmNameEmptyParameter() {
        JvmType actualType = JvmType.fromVendor("");
        Assert.assertSame(JvmType.UNKNOWN, actualType);
    }

    @Test
    public void fromVmNameValidParameter() {
        final String openJdkJavaVmName = "OpenJDK 64-Bit Server VM";
        final String oracleJavaVmName = "Java HotSpot(TM) 64-Bit Server VM";
        final String ibmJavaVmName = "IBM J9 VM";
        Assert.assertSame(JvmType.OPENJDK, JvmType.fromVmName(openJdkJavaVmName));
        Assert.assertSame(JvmType.ORACLE, JvmType.fromVmName(oracleJavaVmName));
        Assert.assertSame(JvmType.IBM, JvmType.fromVmName(ibmJavaVmName));
    }

    @Test
    public void fromVmNameInvalidParameter() {
        Assert.assertSame(JvmType.UNKNOWN, JvmType.fromVmName("Some Invalid Parameter"));
    }
}

