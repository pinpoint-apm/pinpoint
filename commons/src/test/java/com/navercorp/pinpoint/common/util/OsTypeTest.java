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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roy Kim
 */
public class OsTypeTest {

    @Test
    public void fromOsNameNullParameter() {
        OsType actualType = OsType.fromOsName(null);
        Assert.assertSame(OsType.UNKNOWN, actualType);
    }

    @Test
    public void fromOsNameEmptyParameter() {
        OsType actualType = OsType.fromOsName("");
        Assert.assertSame(OsType.UNKNOWN, actualType);
    }

    @Test
    public void fromOsNameValidParameter() {
        final String windowOsName = "Windows 2000";
        final String macOsName = "Mac OS X";
        final String linuxOsName = "Linux";
        final String solarisOsName = "Solaris";
        Assert.assertSame(OsType.WINDOW, OsType.fromOsName(windowOsName));
        Assert.assertSame(OsType.MAC, OsType.fromOsName(macOsName));
        Assert.assertSame(OsType.LINUX, OsType.fromOsName(linuxOsName));
        Assert.assertSame(OsType.SOLARIS, OsType.fromOsName(solarisOsName));
    }

    @Test
    public void fromOsNameInvalidParameter() {
        Assert.assertSame(OsType.UNKNOWN, OsType.fromOsName("Some Invalid Parameter"));
    }
}

