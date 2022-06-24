/*
 * Copyright 2018 Naver Corp.
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

import java.util.EnumSet;

/**
 * @author Roy Kim
 */
public class OsTypeTest {

    @Test
    public void fromVendorNullParameter() {
        OsType actualType = OsType.fromVendor(null);
        Assertions.assertSame(OsType.UNKNOWN, actualType);
    }

    @Test
    public void fromVendorEmptyParameter() {
        OsType actualType = OsType.fromVendor("");
        Assertions.assertSame(OsType.UNKNOWN, actualType);
    }

    @Test
    public void fromVendorValidParameter() {
        Assertions.assertSame(OsType.WINDOW, OsType.fromVendor("window"));
        Assertions.assertSame(OsType.MAC, OsType.fromVendor("mac"));
        Assertions.assertSame(OsType.LINUX, OsType.fromVendor("linux"));
        Assertions.assertSame(OsType.SOLARIS, OsType.fromVendor("SOLARIS"));
        Assertions.assertSame(OsType.AIX, OsType.fromVendor("aix"));
        Assertions.assertSame(OsType.HP_UX, OsType.fromVendor("HP_Ux"));
        Assertions.assertSame(OsType.BSD, OsType.fromVendor("bsd"));
    }

    @Test
    public void fromVendorInvalidParameter() {
        Assertions.assertSame(OsType.UNKNOWN, OsType.fromVendor("Some Invalid Parameter"));
    }

    @Test
    public void fromOsNameNullParameter() {
        OsType actualType = OsType.fromOsName(null);
        Assertions.assertSame(OsType.UNKNOWN, actualType);
    }

    @Test
    public void fromOsNameEmptyParameter() {
        OsType actualType = OsType.fromOsName("");
        Assertions.assertSame(OsType.UNKNOWN, actualType);
    }

    @Test
    public void fromOsNameValidParameter() {
        final String windowOsName = "Windows 2000";
        final String macOsName = "Mac OS X";
        final String linuxOsName = "Linux";
        final String solarisOsName = "Solaris";
        final String hpOsName = "HP-Ux";
        Assertions.assertSame(OsType.WINDOW, OsType.fromOsName(windowOsName));
        Assertions.assertSame(OsType.MAC, OsType.fromOsName(macOsName));
        Assertions.assertSame(OsType.LINUX, OsType.fromOsName(linuxOsName));
        Assertions.assertSame(OsType.SOLARIS, OsType.fromOsName(solarisOsName));
        Assertions.assertSame(OsType.HP_UX, OsType.fromOsName(hpOsName));
    }

    @Test
    public void fromOsNameInvalidParameter() {
        Assertions.assertSame(OsType.UNKNOWN, OsType.fromOsName("Some Invalid Parameter"));
    }

    @Test
    public void testInvalidOSName() {

        EnumSet<OsType> OS_TYPE = EnumSet.allOf(OsType.class);

        for (OsType osType : OS_TYPE) {
            for (OsType osType2 : OS_TYPE) {
                if (osType.equals(osType2)) {
                    continue;
                }
                if (osType == OsType.UNKNOWN || osType2 == OsType.UNKNOWN) {
                    continue;
                }

                if (osType.getInclusiveString().toLowerCase().contains(osType2.getInclusiveString().toLowerCase())) {
                    Assertions.fail("May cause duplicate Os types, check list of OsType");
                }
            }
        }
    }
}

